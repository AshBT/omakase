# [[ .ClusterName ]]
Welcome to your cluster! We've customized several [terraform](http://terraform.io) files for you. This cluster is compatible with omakase.

## Dependencies
You'll need to install `terraform`:
```
brew install terraform
```

## Launching
To launch the cluster, ensure you have your AWS credentials defined in the environment. You can check to see what will happen with
```
terraform plan
```
You can then launch the cluster with
```
terraform apply
```

## Adding services
You can use plain-old `fleetctl` to add services, but omakase can do it for you, too!
