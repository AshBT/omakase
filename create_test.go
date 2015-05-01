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
  assetName, err := getAssetName("templates/README.md")
  e.Expect(err).
    Equals(nil)

  e.Expect(assetName).
    Equals("README.md")
}

func TestCreateTemplate(t *testing.T) {
  data := `# test template {{ ignore these }}
[[ .DiscoveryURL ]]
[[ .ClusterName ]]
[[ .ClusterRoot ]]
[[ .PublicKeyPath ]]
[[ .PrivateKeyPath ]]
[[ .CloudConfigPath ]]`
  expected := `# test template {{ ignore these }}
foobar
bar
test_dir/bar
test_dir/bar/bar.pub
test_dir/bar/bar
test_dir/bar/cloud-config`

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
  defer os.RemoveAll("test_dir")

  e := expectations.New(t)
  e.Label("README.md").
    Expect("test_dir/bar/README.md").
    FileExists()

  e.Label("cloud-config").
    Expect("test_dir/bar/cloud-config").
    FileExists()

  e.Label("omakase.tf").
    Expect("test_dir/bar/omakase.tf").
    FileExists()

  e.Label("variables.tf").
    Expect("test_dir/bar/variables.tf").
    FileExists()

  e.Label("Public key: bar.pub").
    Expect("test_dir/bar/bar.pub").
    FileExists()

  e.Label("Private key: bar").
    Expect("test_dir/bar/bar").
    FileExists()
}
