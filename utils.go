package omakase

import "log"

func check(e error) {
  if e != nil {
      log.Fatal(e)
  }
}