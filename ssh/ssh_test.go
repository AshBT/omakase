package ssh

import (
  "bytes"
  "testing"
  "github.com/qadium/omakase/expectations"
)

func TestPublicKeyWriter(t *testing.T) {
  e := expectations.New(t)

  keyPair := &KeyPair{[]byte("private"), []byte("public")}

  // ensure it's written properly
  var b bytes.Buffer
  err := keyPair.WritePublicKey(&b)

  if err != nil {
    t.Error(err)
  }

  e.Label("Public key").
    Expect(b.String()).
    Equals("public")
}

func TestPrivateKeyWriter(t *testing.T) {
  e := expectations.New(t)

  keyPair := &KeyPair{[]byte("private"), []byte("public")}

  // ensure it's written properly
  var b bytes.Buffer
  err := keyPair.WritePrivateKey(&b)

  if err != nil {
    t.Error(err)
  }

  e.Label("Private key").
    Expect(b.String()).
    Equals("private")
}
