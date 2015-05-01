package omakase

import (
  "testing"
  "text/template"
  "bytes"
  "os"
  "github.com/qadium/omakase/expectations"
)

func TestGetAssetName(t *testing.T) {
  e := expectations.New(t)
  e.Expect(getAssetName("templates/README.md")).
    Equals("README.md")
}

func TestCreateTemplate(t *testing.T) {
  data := `# test template {{ ignore these }}
[[ .DiscoveryURL ]]
[[ .ClusterName ]]
[[ .PublicKeyPath ]]
[[ .CloudConfigPath ]]`
  expected := `# test template {{ ignore these }}
foobar
bar
my/fake/home/bar/bar.pub
my/fake/home/bar/cloud-config`

  buf := new(bytes.Buffer)
  ctx := TestContext()
  e := expectations.New(t)

  tmpl := createTemplate("doesn't matter")
  tmpl = template.Must(tmpl.Parse(data))
  handleTemplate(ctx, tmpl, buf)

  e.Expect(buf.String()).
    Equals(expected)
}

func TestCreate(t *testing.T) {
  ctx := TestContext()
  Create(ctx)

  defer os.RemoveAll("bar")

  e := expectations.New(t)
  e.Label("README.md").
    Expect("bar/README.md").
    FileExists()

  e.Label("cloud-config").
    Expect("bar/cloud-config").
    FileExists()

  e.Label("bar.tf").
    Expect("bar/bar.tf").
    FileExists()

  e.Label("variables.tf").
    Expect("bar/variables.tf").
    FileExists()

  e.Label("Public key: bar.pub").
    Expect("bar/bar.pub").
    FileExists()

  e.Label("Private key: bar").
    Expect("bar/bar").
    FileExists()
}
