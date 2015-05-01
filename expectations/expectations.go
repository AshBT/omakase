package expectations

import (
  "os"
  "testing"
)

type testWrapper struct {
  t *testing.T
  label string
  expect interface{}  // i just want something that implements "=="! argh
}

func New(t *testing.T) (*testWrapper){
  // Return a new expectation structure
  return &testWrapper{t, "", ""}
}

// Simple string comparison for testing
func (r *testWrapper) Equals(other interface{}) {
  if r.expect != other {
    if r.label != "" {
      r.t.Errorf("%s: Expected '%s' but got '%s' instead.", r.label, r.expect, other)
    } else {
      r.t.Errorf("Expected '%s' but got '%s' instead.", r.expect, other)
    }
  }
}

func (r *testWrapper) FileExists() {
  // r.expect is an interface{} (basically, a void *[?])
  // r.expect.(string) is a type assertion saying that it should be a string.
  if _, err := os.Stat(r.expect.(string)); os.IsNotExist(err) {
    r.t.Errorf("no such file or directory: %s", r.expect)
  }
}

// Simple expectation
func (r *testWrapper) Expect(this interface{}) (* testWrapper) {
  r.expect = this
  return r
}

// Simple laeler for better error messages
func (r *testWrapper) Label(label string) (* testWrapper) {
  r.label = label
  return r
}
