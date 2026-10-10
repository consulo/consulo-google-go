module example.com/single

go 1.22

require example.org/greeting v1.0.0

replace example.org/greeting => ./third_party/greeting
