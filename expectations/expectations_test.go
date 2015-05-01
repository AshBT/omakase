package expectations

import (
  "os"
  "testing"
)

func TestNew(t *testing.T) {
  t0 := new(testing.T)
  e := New(t0)

  if e.t != t0 {
    t.Error("The tester should be assigned to the struct!")
  }

  if e.label != "" {
    t.Error("The label should be empty!")
  }

  if e.expect != nil {
    t.Error("The expected thing should be nil")
  }
}

func TestLabel(t *testing.T) {
  t0 := new(testing.T)

  expected := "hello, world"
  e := New(t0).Label(expected)

  if e.label != expected {
    t.Errorf("The label is '%s', but we expected '%s'", e.label, expected)
  }
}

func TestExpect(t *testing.T) {
  t0 := new(testing.T)

  e := New(t0).Expect(nil)

  if e.expect != nil {
    t.Error("SimpleExpect: The expected thing should be nil")
  }

  e.Expect("a").Expect(2).Expect(3)
  if e.expect != 3 {
    t.Error("ChainedExpects: The expected thing should be 3.")
  }

}

func TestIntEquals(t *testing.T) {
  t0 := new(testing.T)

  e := New(t0)

  e.Expect(3).Equals(3)
  if e.t.Failed() {
    t.Error("Int comparison should work!")
  }
}

func TestStringEquals(t *testing.T) {
  t0 := new(testing.T)

  e := New(t0)
  e.Expect("abc").Equals("abc")
  if e.t.Failed() {
    t.Error("String comparison should work!")
  }
}

func TestNilEquals(t *testing.T) {
  t0 := new(testing.T)
  e := New(t0)
  e.Expect(nil).Equals(nil)
  if e.t.Failed() {
    t.Error("Nil comparison should work!")
  }
}


func TestIntNotEquals(t *testing.T) {
  t0 := new(testing.T)
  e := New(t0)

  e.Label("NotEquals").Expect(3).Equals(5)
  if !e.t.Failed() {
    t.Error("Test should fail when comparing unequal things!")
  }
}

func TestTypeNotEquals(t *testing.T) {
  t0 := new(testing.T)
  e := New(t0)

  e.Expect(nil).Equals("foobar")
  if !e.t.Failed() {
    t.Error("Test should fail when comparing dissimilar types!")
  }
}

func TestFileExists(t *testing.T) {
  t0 := new(testing.T)
  e := New(t0)

  _, err := os.Create("foobar")
  defer os.Remove("foobar")
  if err != nil {
    t.Error(err)
  }

  e.Expect("foobar").FileExists()
  if e.t.Failed() {
    t.Error("Test should *not* fail when the file exists!")
  }
}

func TestFileNotExists(t *testing.T) {
  t0 := new(testing.T)
  e := New(t0)

  e.Expect("hello").FileExists()
  if !e.t.Failed() {
    t.Error("Test should fail when a file doesn't exists!")
  }
}
