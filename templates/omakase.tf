# access keys and region are sourced from environment variables
provider "aws" {
  access_key = "${var.aws_access_key}"
  secret_key = "${var.aws_secret_key}"
  region = "${var.aws_region}"
}

# this is the key pair used to access our instance
resource "aws_key_pair" "core" {
  key_name = "[[ .ClusterName ]]-key"
  public_key = "${file("[[ .PublicKeyPath ]]")}"
}

# this is the template for the cloud-config file
resource "template_file" "cloud_config" {
  filename = "[[ .CloudConfigPath ]]"
  vars {
    region = "${var.aws_region}"
    access_key = "${var.aws_access_key}"
    secret_key = "${var.aws_secret_key}"
    num_servers = "${var.count}"
  }
}

# this is the default omakase security group
resource "aws_security_group" "sg" {
    name = "ok-sg-[[ .ClusterName ]]"
    description = "omakase: Used for the [[ .ClusterName ]] security group"

    # SSH access from anywhere
    ingress {
      from_port = 22
      to_port = 22
      protocol = "tcp"
      cidr_blocks = ["0.0.0.0/0"]
    }

    # HTTP access from anywhere
    ingress {
      from_port = 80
      to_port = 80
      protocol = "tcp"
      cidr_blocks = ["0.0.0.0/0"]
    }

    # All instances in this security group can talk to each other
    ingress {
      from_port = 0
      to_port = 0
      protocol = "-1"
      self = true
    }

    # Access to all external services
    egress {
      from_port = 0
      to_port = 0
      protocol = "-1"
      cidr_blocks = ["0.0.0.0/0"]
    }

    tags {
      Name = "ok-sg-[[ .ClusterName ]]"
    }
}

# is there a way to have a generic host and only mount to specific
# nodes? at the very least, we need
#    consul server nodes
#    db nodes
#    consul client nodes
#
resource "aws_instance" "coreos_host" {
  ami = "${lookup(var.aws_amis, var.aws_region)}"
  instance_type = "m3.medium"
  security_groups = ["${aws_security_group.sg.name}"]
  key_name = "${aws_key_pair.core.key_name}"
  user_data = "${template_file.cloud_config.rendered}"
  tags {
    Project = "Memex"
    Name = "[[ .ClusterName ]]${count.index}"
    Manager = "omakase"
  }
  count = "${var.count}"
  ebs_block_device = {
    device_name = "/dev/sdj"
    snapshot_id = "snap-7f73fc47"
    volume_type = "io1"
    volume_size = 1000
    iops = 3000
    delete_on_termination = false
  }
}

# output the ips
output "ips" {
  value = ["${aws_instance.coreos_host.*.public_ip}"]
}
