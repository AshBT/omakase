package main

import (
  "os"
  "fmt"
  "github.com/codegangsta/cli"
  ok "github.com/qadium/omakase"
)

func main() {
  versionString := versionString()
  app := cli.NewApp()
  app.Name = "omakase"
  app.Usage = "a command line tool to manage CoreOS clusters"
  app.Version = versionString
  app.Commands = []cli.Command {
    {
      Name:      "create",
      Usage:     "create a cluster managed by omakase",
      Action: func(c *cli.Context) {
        args := c.Args()
        if len(args) != 1 {
          fmt.Println("'omakase create' requires a cluster 'name'")
          return
        }
        name := c.Args().First()
        ctx := ok.NewEtcdContext(name)
        ok.Create(ctx)
      },
    },
    {
      Name:   "version",
      Usage:  "more detailed version information for omakase",
      Action: func(c *cli.Context) {
        fmt.Println("Omakase version:", versionString)
        fmt.Println("Git commit:", GitCommit)
      },
    },
  }
  app.Run(os.Args)
}


func versionString() string {
  versionString := Version
  if VersionPrerelease != "" {
    versionString += "-" + VersionPrerelease
  }
  return versionString
}
