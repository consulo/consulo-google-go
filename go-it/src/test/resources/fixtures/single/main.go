package main

import (
	"fmt"

	"example.com/single/internal/version"
	"example.org/greeting"
)

func main() {
	fmt.Println(greeting.Hello(), version.Version)
}
