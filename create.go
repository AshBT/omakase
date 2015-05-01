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
  log.Printf("==> Specializing templates for %s\n", ctx.ClusterName)
  if err := handleTemplates(ctx); err != nil {
    log.Fatal(err)
  }
}

func handleTemplates(c *Context) error {
  for _, asset := range AssetNames() {
    name, err := getAssetName(asset)
    if err != nil {
      return err
    }

    log.Printf("    Writing %s", name)
    // create the template
    tmpl := createTemplate(name)

    // get the template string
    bytes, err := Asset(asset)
    if err != nil {
      return err
    }
    data := string(bytes)

    // parse the template
    tmpl, err = tmpl.Parse(data)
    if err != nil {
      return err
    }

    // create a file for the template
    filename := fmt.Sprintf("%s/%s", c.ClusterName, name)
    file, err := os.Create(filename)
    if err != nil {
      return err
    }

    // handle the template
    if err := handleTemplate(c, tmpl, file); err != nil {
      return err
    }
  }
  return nil
}

func handleTemplate(c *Context, tmpl *template.Template, writer io.Writer) error {
  if err := tmpl.Execute(writer, *c); err != nil {
    return err
  }
  return nil
}

func getAssetName(asset string) (string, error) {
  info, err := AssetInfo(asset)
  if err != nil {
    return "", err
  }
  return info.Name(), nil
}

func createTemplate(name string) *template.Template {
  // use [[, ]] for templates so we can also embed other templates
  return template.New(name).Delims("[[", "]]")
}
