# Whether to build debug
GO_BINDATA_DEBUG=true

.PHONY: build debug release all clean test
all: debug

debug: GO_BINDATA_DEBUG=true
debug: build test

release: GO_BINDATA_DEBUG=false
release: build test

bindata.go:
	go-bindata -debug=$(GO_BINDATA_DEBUG) -pkg="omakase" templates

build: bindata.go
	sh scripts/build.sh

test: bindata.go
	go test -cover ./...

clean:
	rm bindata.go omakase
