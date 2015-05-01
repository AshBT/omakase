package omakase

import "testing"

func TestNewContext(t *testing.T) {
  ctx := TestContext()

  e := Expectations(t)

  e.Label("DiscoveryURL").
    Expect(ctx.DiscoveryURL).
    Equals("foobar")

  e.Label("ClusterName").
    Expect(ctx.ClusterName).
    Equals("bar")

  e.Label("PublicKeyPath").
    Expect(ctx.PublicKeyPath).
    Equals("my/fake/home/bar/bar.pub")

  e.Label("CloudConfigPath").
    Expect(ctx.CloudConfigPath).
    Equals("my/fake/home/bar/cloud-config")
}
