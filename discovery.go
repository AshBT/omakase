package omakase

import (
  "net/http"
  "io/ioutil"
)

type Discovery interface {
  discover() string
}

type Etcd struct {
  httpEndpoint string
}

func (e *Etcd) discover() string {
  resp, err := http.Get(e.httpEndpoint + "/new")
  check(err)

  defer resp.Body.Close()
  contents, err := ioutil.ReadAll(resp.Body)
  check(err)

  return string(contents)
}
