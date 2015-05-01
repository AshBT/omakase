package ssh

// generate SSH public and private key pair using golang's crypto
// no unit tests for these...

import (
  "crypto/rsa"
  "crypto/rand"
  "crypto/x509"
  "encoding/pem"
  "golang.org/x/crypto/ssh"
  "io"
)

type KeyPair struct {
  PrivateKey []byte // pem encoded private key
  PublicKey []byte  // OpenSSH compatible public key
}

func GenerateKeyPair() (*KeyPair, error) {
  priv, err := makeKeyPair()
  if err != nil {
    return nil, err
  }

  privateKeyBytes := encodePrivateKey(priv)
  publicKeyBytes, err := encodePublicKey(priv)
  if err != nil {
    return nil, err
  }

  return &KeyPair{privateKeyBytes, publicKeyBytes}, nil
}

func (k *KeyPair) WritePublicKey(writer io.Writer) error {
  _, err := writer.Write(k.PublicKey)
  return err
}

func (k *KeyPair) WritePrivateKey(writer io.Writer) error {
  _, err := writer.Write(k.PrivateKey)
  return err
}

func makeKeyPair() (*rsa.PrivateKey, error) {
  // lifted from https://github.com/golang-samples/cipher/blob/master/crypto/rsa_keypair.go
  priv, err := rsa.GenerateKey(rand.Reader, 2048)
  if err != nil {
    return nil, err
  }

  if err = priv.Validate(); err != nil {
    return nil, err
  }
  return priv, nil
}

func encodePrivateKey(priv *rsa.PrivateKey) []byte {
  // encode private key in DER format
  privDer := x509.MarshalPKCS1PrivateKey(priv)

  privBlk := pem.Block {
    Type: "RSA PRIVATE KEY",
    Headers: nil,
    Bytes: privDer,
  }

  return pem.EncodeToMemory(&privBlk)
}

func encodePublicKey(priv *rsa.PrivateKey) ([]byte, error) {
  pub := priv.PublicKey
  pubKey, err := ssh.NewPublicKey(&pub)
  if err != nil {
    return nil, err
  }

  return ssh.MarshalAuthorizedKey(pubKey), nil
}
