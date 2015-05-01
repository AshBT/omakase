package omakase

import (
  "fmt"
  "os"
  "log"
)

type Context struct {
  // capitalized members are accessible in templates
  DiscoveryURL    string    // discovery url for this context
  ClusterName     string    // the cluster name
  PublicKeyPath   string    // absolute path to the public key for ssh
  CloudConfigPath string    // absolute path to generated cloud-config
}

func NewContext(e Discovery, name string, workingdir string) (*Context) {
  // Generic context creator
  cloudConfig := fmt.Sprintf("%s/%s/cloud-config", workingdir, name)
  pubkeyPath := fmt.Sprintf("%s/%s/%s.pub", workingdir, name, name)
  return &Context{e.discover(), name, pubkeyPath, cloudConfig}
}

func NewEtcdContext(name string) (*Context) {
  // Context creator for default etcd discovery
  etcd := &Etcd {"https://discovery.etcd.io"}
  cwd, err := os.Getwd();
  if err != nil {
    log.Fatal(err)
  }
  return NewContext(etcd, name, cwd)
}
