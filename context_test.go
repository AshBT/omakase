package omakase

import "testing"

type MockEtcd struct {}

func (e *MockEtcd) discover() string {
  return "foobar"
}

func TestNewContext(t *testing.T) {
  // ensure that MockEtcd implements the discovery interface
  // if it doesn't, the compiler will crap out.
  var e Discovery = (*MockEtcd)(nil)

  ctx := NewContext(e, "bar")

  if ctx.DiscoveryURL != "foobar" {
    t.Errorf("DiscoveryURL: Expected 'foobar' but got '%s' instead.", ctx.DiscoveryURL)
  }

  if ctx.ClusterName != "bar" {
    t.Errorf("ClusterName: Expected 'bar' but got '%s' instead.", ctx.ClusterName)
  }
}
