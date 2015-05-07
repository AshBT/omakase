# Whether to build debug
GO_BINDATA_DEBUG=true

.PHONY: build debug release all clean test
all: debug

debug: GO_BINDATA_DEBUG=true
debug: build test

release: GO_BINDATA_DEBUG=false
release: build test

data/data.go:
	mkdir -p data && \
	go-bindata -debug=$(GO_BINDATA_DEBUG) -pkg="data" -o="data/data.go" templates

build: data/data.go
	sh scripts/build.sh

test: data/data.go
	go test -cover ./...

clean:
	rm data/* omakase && \
	rmdir data && \
	go clean
