# access keys and region are sourced from environment variables
provider "aws" {
  region = "${var.aws_region}"
}

# this is the key pair used to access our instance
resource "aws_key_pair" "core" {
  key_name = "coreos-key"
  public_key = "${file("[[ .PublicKeyPath ]]")}"
}

# this is the default omakase security group
resource "aws_security_group" "omakase" {
    name = "omakase"
    description = "Used for the omakase security group"

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
      to_port = 65535
      protocol = "-1"
      self = true
    }
}

resource "aws_instance" "coreos_host" {
    ami = "${lookup(var.aws_amis, var.aws_region)}"
    instance_type = "m3.medium"
    security_groups = ["${aws_security_group.omakase.name}"]
    key_name = "${aws_key_pair.core.key_name}"
    user_data = "${file("[[ .CloudConfigPath ]]")}"
    tags {
      Project = "Memex"
      Name = "[[ .ClusterName ]]${count.index}"
    }
    count = "${var.count}"
}
