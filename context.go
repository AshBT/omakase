package omakase

import (
  "fmt"
  "os"
)

type Context struct {
  // capitalized members are accessible in templates
  DiscoveryURL    string    // discovery url for this context
  ClusterName     string    // the cluster name
  PublicKeyPath   string    // path to the public key for ssh access
  CloudConfigPath string    // path to generated cloud-config file    
}

func NewContext(e Discovery, name string) (*Context) {
  cwd, err := os.Getwd()
  check(err)
  // Generic context creator
  return &Context{e.discover(), name, "foo", fmt.Sprintf("%s/%s/cloud-config", cwd, name)}
}

func NewEtcdContext(name string) (*Context) {
  // Context creator for default etcd discovery
  etcd := &Etcd {"https://discovery.etcd.io"}
  return NewContext(etcd, name)
}
