package omakase

import (
  "text/template"
  "github.com/qadium/omakase/ssh"
  "os"
  "log"
  "io"
  "fmt"
)

func Create(ctx *Context) {
  log.Printf("==> Creating folder '%s'\n", ctx.ClusterRoot)
  err := os.MkdirAll(ctx.ClusterRoot, 0700)
  if err != nil {
    log.Fatal(err)
  }

  log.Printf("==> Specializing templates for '%s'\n", ctx.ClusterName)
  if err := handleTemplates(ctx); err != nil {
    log.Fatal(err)
  }

  log.Printf("==> Creating SSH key pairs for '%s'\n", ctx.ClusterName)
  keypair, err := ssh.GenerateKeyPair()
  if err != nil {
    log.Fatal(err)
  }

  log.Printf("==> Writing SSH key pairs for '%s'\n", ctx.ClusterName)
  if err := writeKeyPair(ctx, keypair); err != nil {
    log.Fatal(err)
  }
}

func writeKeyPair(ctx *Context, keyPair *ssh.KeyPair) error {
  log.Printf("    Writing public key '%s'", ctx.PublicKeyPath)
  file, err := os.Create(ctx.PublicKeyPath)
  defer file.Close()
  if err != nil {
    return err
  }

  if err := keyPair.WritePublicKey(file); err != nil {
    return err
  }

  log.Printf("    Writing private key '%s'", ctx.PrivateKeyPath)
  file, err = os.Create(ctx.PrivateKeyPath)
  defer file.Close()
  if err != nil {
    return err
  }

  // make the private key read only for this user
  if err := file.Chmod(0600); err != nil {
    return err
  }

  if err := keyPair.WritePrivateKey(file); err != nil {
    return err
  }
  return nil
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
    filename := fmt.Sprintf("%s/%s", c.ClusterRoot, name)
    file, err := os.Create(filename)
    defer file.Close()
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
