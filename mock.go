package omakase

import (
  "os"
  "testing"
)

type MockEtcd struct {}

func (e *MockEtcd) discover() string {
  return "foobar"
}

func TestContext() (* Context) {
  // ensure that MockEtcd implements the discovery interface
  // if it doesn't, the compiler will crap out.
  var d Discovery = (*MockEtcd)(nil)

  return NewContext(d, "bar", "my/fake/home")
}

type R struct {
  t *testing.T
  label string
  expect string
}

func Expectations(t *testing.T) (*R){
  return &R{t, "", ""}
}

// Simple string comparison for testing
func (r *R) Equals(other string) {
  if r.expect != other {
    if r.label != "" {
      r.t.Errorf("%s: Expected '%s' but got '%s' instead.", r.label, r.expect, other)
    } else {
      r.t.Errorf("Expected '%s' but got '%s' instead.", r.expect, other)
    }
  }
}

func (r *R) FileExists() {
  if _, err := os.Stat(r.expect); os.IsNotExist(err) {
    r.t.Errorf("no such file or directory: %s", r.expect)
  }
}

// Simple expectation
func (r *R) Expect(this string) (* R) {
  r.expect = this
  return r
}

// Simple laeler for better error messages
func (r *R) Label(label string) (* R) {
  r.label = label
  return r
}
