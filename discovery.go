package omakase

import (
  "net/http"
  "io/ioutil"
  "log"
)

type Discovery interface {
  discover() string
}

type Etcd struct {
  httpEndpoint string
}

func (e *Etcd) discover() string {
  resp, err := http.Get(e.httpEndpoint + "/new")
  if err != nil {
    log.Fatal(err)
  }

  defer resp.Body.Close()
  contents, err := ioutil.ReadAll(resp.Body)
  if err != nil {
    log.Fatal(err)
  }

  return string(contents)
}
