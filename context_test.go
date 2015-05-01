package omakase

import (
  "testing"
  "github.com/qadium/omakase/expectations"
)

func TestNewContext(t *testing.T) {
  ctx := TestContext()

  e := expectations.New(t)

  e.Label("DiscoveryURL").
    Expect(ctx.DiscoveryURL).
    Equals("foobar")

  e.Label("ClusterName").
    Expect(ctx.ClusterName).
    Equals("bar")

  e.Label("ClusterRoot").
    Expect(ctx.ClusterRoot).
    Equals("test_dir/bar")

  e.Label("PublicKeyPath").
    Expect(ctx.PublicKeyPath).
    Equals("test_dir/bar/bar.pub")

  e.Label("PrivateKeyPath").
    Expect(ctx.PrivateKeyPath).
    Equals("test_dir/bar/bar")

  e.Label("CloudConfigPath").
    Expect(ctx.CloudConfigPath).
    Equals("test_dir/bar/cloud-config")
}
