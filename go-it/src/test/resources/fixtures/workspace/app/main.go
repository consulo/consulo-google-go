package main

import (
	"fmt"

	"example.com/app/internal/config"
	"example.com/lib/util"
	"example.org/greeting"
)

func main() {
	fmt.Println(config.Name(), util.Shout(greeting.Hello()))
}
