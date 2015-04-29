# Omakase
Omakase is a tool to manage and launch services on any cloud backed by CoreOS. It acts like a package manager and handles all the nitty gritty for you (we hope).

Maybe we'll have a registry, maybe we won't.

For now, Omakase comes with a default `cloud-config` for starting your own AWS cluster with CoreOS and properly sets up needed values.

## Hello, world
Create a new folder, say, `hello` and open a new YAML file: `hello.yml`. Copy
```YAML
name: hello
version: 0.0.1-alpha

apps:
# Give the app a name; it will be available via dns as
# hello.service.consul or just 'hello'
- service: hello
  # You can overwrite any default environment variables in the hello app
  env:
    - FOOBAR=foo
    - WORLD=cruel world
  # This is "publicly" available at 'hello.$IP.xip.io' (assuming your
  # port 80 is open).
  http: yes

# The name can also point to a github repository following Omakase
# conventions. This will clone the repository and build a docker
# container for it and launch the service.
- service: https://github.com/qadium/demo
  # You can optionally specify the name used for DNS (instead of
  # demo.service.consul)
  name: mickey

- service: mongodb
  # You can optionally specify resource constraints for the service and
  # Omakase will attempt to ensure that your app is run on a machine
  # that satisfies the constraints or attempt to provision one for you.
  storage: 100G

# A service group treats the individual services as one logical unit.
# The composite services, however, are still individual containers.
- service-group: kafka-coreos
  - service: zk-exhibitor
    name: zookeeper
    instances: 3
  - service: kafka
    instances: 5
```
