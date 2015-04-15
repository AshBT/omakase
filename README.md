# omakase
Omakase is a simple tool to manage and launch services on a CoreOS
cluster in AWS. Omakase also comes with a list of [essential services](#default-services). At the moment, Omakase has been tested on
OSX.

## Installing
Clone this repo

    git clone https://github.com/qadium/omakase

Ensure you have `leiningen`, `fleetctl`, `etcdctl`, and (optionally) `drip` installed locally.

    brew install leiningen fleetctl etcdctl drip

Leiningen is used to build Clojure projects. The `fleetctl` and `etcdctl` tools are used because omakase shells out to these commands for some of its functionality. [Drip](https://github.com/ninjudd/drip) is used to reduce JVM startup times.

Now, run

    lein omakase

This will install `omakase` to the `~/.ok` directory along with a script, `ok`, to `/usr/local/bin`. If you need admin privileges to install the script, you can either use `sudo` or perform a local install.

### Local installation
Manually copy the script `bin/ok` to an executable path and modify the `%%version%%` at the top of the copied `ok` script. It should, at this moment, be `0.0.1-SNAPSHOT`.

### Re-building
If you modify the source code and rebuild via `lein omakase`, make sure you invoke it with `drip kill && lein omakase` instead. This will kill any JVMs that `drip` has left behind. Those JVMs will be using an older version of `omakase` and will *not* reflect changes in your code.

## Usage
Running `ok -h` produces the following:
```shell
Usage: ok [OPTIONS] COMMAND [arg...]

A tool for dynamic clouds.

Options:

  -h, --help
      --version         version information for omakase
  -l, --log-level INFO  level of logging

Commands:
  General:
    run        Run an app with omakase environment variables.
    config     Configure omakase credentials.
    status     List known clouds.
    help       Show help for a command.
    version    Provide versioning information.

  Utilities:
    etcdctl    Forwards command to etcdctl.
    ssh        Prints the ssh command to ssh into (one machine in) the cloud.
    fleet      Forwards command to fleetctl.

  Clusters:
    create     Create the cloud with the name 'cluster-name'.
    land       Shuts down the named cluster.
    launch     Launch a cluster with a given name and number of nodes.

  Services:
    start      Starts a service.

Run 'ok COMMAND --help' for more information on a command.
```

The `--log-level` flag is currently non-functional. The `run` command is deprecated. The `etcdctl` command doesn't always work.

### Additional tools
In the `resources` folder, we also provide additional scripts for interacting with the cluster. These are

  - `ok-connect`, opens an SSH connection to one of your nodes
  - `ok-ssh`, a drop-in replacement for the ssh command that references the identity file in `~/.ok/global-key`
  - `ok-tunnel`, opens a SOCKS5 tunnel to one of your nodes
  - `ok-create`, sets up a remote git repository with a post-receive hook that builds a Docker container automatically

## Examples

### Config
After a clean install, you must set up your AWS credentials *once*

    ok config AWS_ACCESS_KEY AWS_SECRET_KEY

This will generate an SSH key for you as well and store it to `~/.ok/global_key`.

From then on, you no longer need to configure omakase, except for when your credentials change (of course). You can see more details on what the command does via `ok help config`.

### New cluster
To launch a new cluster, simply create one, `ok create foo`, then launch it `ok launch foo`. This will, by default, launch the cluster `foo` with 5 nodes. You can find more information on these commands by typing `ok help create` and `ok help launch`.

A new cluster on AWS (using Amazon EC2 us-west-1) will use `m3.medium` instances.

Once the cluster is up, periodically use `ok fleet foo list-units` to ensure that all the default services are in a running state.

#### Default services
By default, `omakase` will attempt to start the following services for you after the cluster is started:

  - 3 zookeeper nodes, named `zookeeper1`, `zookeeper2`, and `zookeeper3`; these are managed by Netflix's Exhibitor
  - 3 kafka nodes, named `kafka1`, `kafka2`, and `kafka3`
  - a private docker registry, named `ok-registry`

Any machine in your cluster can reach the services simply by using their names.

#### Setting up S3
S3 must be set up manually for zookeeper and the private docker registry to work.

By default, the docker registry and the zookeeper nodes attempt to write to a bucket named `omakase-s3`. You can modify the `resources/docker-registry.service` file and the `resources/zookeeper@.service` file before building to point this to a different bucket.

### Adding services
Once the cluster is up, you can add fleet services to it by running `ok start foo PATH_TO_FLEET_SERVICE`. This will use `fleetctl` under the hood to submit the service. Alternatively, you can use `ok fleet foo start PATH_TO_FLEET_SERVICE` as this will simply forward any commands off the `fleetctl` on the cluster.

### Creating new services
Currently, we do not have a way of creating new services. This is done by hand.

When creating a new service, we must ensure that both a `Dockerfile` and a fleet `.service` file exist in the directory. The `Dockerfile` is necessary to handle all the dependencies and ensure that the built container has all its dependencies. The `.service` file is necessary to run the built Docker container. The `.service` file must refer to the Docker image that was just built.

As an example, here is a generic Dockerfile template for a Python command line app
```dockerfile
FROM ubuntu:saucy

# Install required packages
RUN apt-get update
RUN DEBIAN_FRONTEND=noninteractive apt-get -y install python python-dev

# Install basic applications
RUN apt-get install -y curl

# Install pip
RUN curl https://bootstrap.pypa.io/get-pip.py | python
RUN pip install $MY_APP_DEPENDENCIES

# Add our python app code to the image
RUN mkdir -p /app
ADD . /app
WORKDIR /app
# expose any ports your service might need
EXPOSE ...

# Set the default command to execute
CMD /bin/bash -c "python $MY_APP.py"
```
Once this Dockerfile is in your repository, you can use the `ok-create` utility to create a remote repository:
```shell
./ok-create foo $MY_APP
```
This will create a remote `foo` that points to the AWS node. After committing your code, push to omakase with
```shell
GIT_SSH=ok-ssh git push foo master
```
The `GIT_SSH` environment variable is required so that `git` uses ssh with our omakase identity at `~/.ok/global-key`. This should attempt to build the Docker container and tag it with `ok-registry.service.consul/MY_APP`.

The corresponding service file might be
```ini
[Unit]
Description=DESCRIBE YOUR SERVICE
After=docker.service
After=consul-server.service
Requires=docker.service

[Service]
TimeoutStartSec=0
ExecStartPre=-/usr/bin/docker kill $MY_APP
ExecStartPre=-/usr/bin/docker rm $MY_APP
ExecStartPre=/usr/bin/docker pull ok-registry.service.consul/$MY_APP
ExecStart=/usr/bin/docker run --name $MY_APP \
    -e SERVICE_NAME=$MY_APP \
    -e SERVICE_TAGS=... \
    ok-registry.service.consul/$MY_APP

ExecStop=/usr/bin/docker stop $MY_APP
```
If your service is an http service, set `SERVICE_TAGS=http`. Omakase will automatically discover it and other services can reference this service with the name `$MY_APP` (its full domain is `$MY_APP.service.consul`, but `service.consul` is added to every machine's search domain).

After saving the file to, say, `$MY_APP.service`, you are now ready to launch it with
```shell
ok start foo $MY_APP
```
or
```shell
ok fleet foo start $MY_APP
```

## Troubleshooting

1. What's `ok run` for?

  The IPs of one of the Kafka brokers and one of the Zookeeper nodes used to be stored in environment variables named `KAFKA_BROKER` and `ZOOKEEPER_SERVER`. These environment variables are possibly still accessible in containers.

  To support local development, `ok run` would inject the variables into an environment and run your code with the new environment. Thus, you could run a simple Python script with
  ```shell
  ok run foo python -c 'import os; print os.environ'
  ```
  This would give you access to the IPs in your code. Compare to
  ```shell
  python -c 'import os; print os.environ'
  ```
  Or `ok run foo env` versus `env`.

  Once the services became addressable by *name* (e.g., `zookeeper1`), this functionality became obsolete.
  
2. After launching, omakase complains about not being able to reach fleet.

  You might have to wait some time before the fleet endpoint is available. To manually launch the default services, follow these steps (assuming you created a cluster named foo)
  ```shell
  cd ~/.ok/foo
  ok fleet foo start docker-registry.service zookeeper@{1..3}.service zookeeper-discovery@{1..3} kafka-broker@{1..3}.service kafka-broker-discovery@{1..3}.service
  ```
  This will manually launch the default services.

3. I have problems with Kafka.

  Sometimes Kafka starts before the Zookeeper nodes are ready. Just for good measure, ensure the zookeeper nodes are up. Then, you can run
  ```shell
  ok fleet foo unload kafka-broker@{1..3}
  ok fleet foo start kafka-broker@{1..3}
  ```
  This will stop the brokers and relaunch them.

  If you're connected to the cluster through a SOCKS proxy and have set up your browser to also proxy DNS requests, you can also point your browser to `http://zookeeper1.service.consul:8181` to see the status of your zookeeper nodes and if any Kafka nodes are connected to them.

4. I have some other issue.

  There's always the nuclear option:
  ```shell
  ok land foo
  ```
  This will destroy the cluster. You can then run
  ```shell
  # rewrite the default service files
  ok create foo
  # relaunch the cluster
  ok launch foo
  ```

5. Help.

  You can always contact us via the [issues page](https://github.com/qadium/omakase/issues) or [email](mailto://eric@qadium.com).

## License

Copyright © 2015 Qadium, Inc

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
