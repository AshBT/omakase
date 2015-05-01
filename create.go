package omakase

import (
  "text/template"
  "os"
  "log"
  "io"
  "fmt"
)

func Create(ctx *Context) {
  log.Printf("==> Creating folder %s\n", ctx.ClusterName)
  os.Mkdir(ctx.ClusterName, 0700)
  // should create a subdirectory
  // and copy over
  //   cloud-config
  //   variables.tf
  //   (name).tf
  log.Printf("==> Specializing templates for %s\n", ctx.ClusterName)
  handleTemplates(ctx)
}

func handleTemplates(c *Context) {
  for _, asset := range AssetNames() {
    name := getAssetName(asset)
    log.Printf("    Writing %s", name)

    // create the template
    tmpl := createTemplate(name)
    // get the template string
    data := string(MustAsset(asset))
    // parse the template
    tmpl, err := tmpl.Parse(data)
    check(err)
    // create a file for the template
    filename := fmt.Sprintf("%s/%s", c.ClusterName, name)
    file, err := os.Create(filename)
    check(err)
    handleTemplate(c, tmpl, file)
  }
}

func handleTemplate(c *Context, tmpl *template.Template, writer io.Writer) {
  err := tmpl.Execute(writer, *c)
  check(err)
}

func getAssetName(asset string) string {
  info, err := AssetInfo(asset)
  check(err)
  return info.Name()
}

func createTemplate(name string) *template.Template {
  // use [[, ]] for templates so we can also embed other templates
  return template.New(name).Delims("[[", "]]")
}
