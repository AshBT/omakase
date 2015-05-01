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
  ClusterRoot     string    // the directory for the cluster
  PublicKeyPath   string    // absolute path to the public key for ssh
  PrivateKeyPath  string    // absolute path to the private key for ssh
  CloudConfigPath string    // absolute path to generated cloud-config
}

func NewContext(e Discovery, name string, workingDir string) (*Context) {
  // Generic context creator
  clusterRoot := fmt.Sprintf("%s/%s", workingDir, name)
  cloudConfig := fmt.Sprintf("%s/cloud-config", clusterRoot)
  pubkeyPath := fmt.Sprintf("%s/%s.pub", clusterRoot, name)
  privkeyPath := fmt.Sprintf("%s/%s", clusterRoot, name)
  return &Context{
    DiscoveryURL: e.discover(),
    ClusterName: name,
    ClusterRoot: clusterRoot,
    PublicKeyPath: pubkeyPath,
    PrivateKeyPath: privkeyPath,
    CloudConfigPath: cloudConfig,
  }
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
