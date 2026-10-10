package util

import "testing"

func TestShout(t *testing.T) {
	if got := Shout("hello"); got != "HELLO!" {
		t.Fatalf("Shout(hello) = %q", got)
	}
}
