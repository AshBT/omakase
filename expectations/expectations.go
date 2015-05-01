package expectations

import (
  "os"
  "testing"
)

type testWrapper struct {
  t *testing.T
  label string
  expect string
}

func New(t *testing.T) (*testWrapper){
  // Return a new expectation structure
  return &testWrapper{t, "", ""}
}

// Simple string comparison for testing
func (r *testWrapper) Equals(other string) {
  if r.expect != other {
    if r.label != "" {
      r.t.Errorf("%s: Expected '%s' but got '%s' instead.", r.label, r.expect, other)
    } else {
      r.t.Errorf("Expected '%s' but got '%s' instead.", r.expect, other)
    }
  }
}

func (r *testWrapper) FileExists() {
  if _, err := os.Stat(r.expect); os.IsNotExist(err) {
    r.t.Errorf("no such file or directory: %s", r.expect)
  }
}

// Simple expectation
func (r *testWrapper) Expect(this string) (* testWrapper) {
  r.expect = this
  return r
}

// Simple laeler for better error messages
func (r *testWrapper) Label(label string) (* testWrapper) {
  r.label = label
  return r
}
