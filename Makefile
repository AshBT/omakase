GO_BINDATA_DEBUG=false

.PHONY: build debug release all clean test
all: debug

debug: GO_BINDATA_DEBUG=true
debug: build

release: GO_BINDATA_DEBUG=false
release: build

bindata.go:
	echo $(GO_BINDATA_DEBUG) && \
	go-bindata -debug=$(GO_BINDATA_DEBUG) -pkg="omakase" templates

build: bindata.go
	go build -v && \
	cd cmd/omakase && \
	go build -v && \
	cd ../.. && \
	cp cmd/omakase/omakase .

test:
	go test -v

clean:
	rm bindata.go omakase
