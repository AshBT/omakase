variable "aws_region" {
    description = "The AWS region to create things in."
    default = "us-west-1"
}

# CoreOS Stable Channel
variable "aws_amis" {
    description = "AMIs for CoreOS stable channel"
    default = {
        us-west-1 = "ami-43f91b07"
        us-west-2 = "ami-37280207"
        us-east-1 = "ami-d2033bba"
    }
}

variable "count" {
  description = "The number of nodes to bring up."
  default = 3
}
