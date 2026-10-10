package util

import (
	"strings"

	"example.org/greeting"
)

func Shout(text string) string { return strings.ToUpper(text) + greeting.Mark }
