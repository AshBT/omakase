package omakase


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
