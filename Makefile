# Whether to build debug
GO_BINDATA_DEBUG=false

.PHONY: build debug release all clean test
all: debug

debug: GO_BINDATA_DEBUG=true
debug: build

release: GO_BINDATA_DEBUG=false
release: build

bindata.go:
	go-bindata -debug=$(GO_BINDATA_DEBUG) -pkg="omakase" templates

build: bindata.go
	sh scripts/build.sh

test: bindata.go
	go test -v -cover ./...

clean:
	rm bindata.go omakase
