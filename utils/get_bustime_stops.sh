#!/bin/bash

# Format
# insert into stop values ('1168', '1-й км', 'Пионерскую', 56.863213, 60.61197, 'tram');
# link: https://www.bustime.ru/ekaterinburg/stop/id/98120/


LINK="https://www.bustime.ru"
CITY=$1
startID=$2
stopID=$3

function using
{
        echo "-- HELP --"
        echo "Script for getting stop information from bustime"
	echo "	args: <city> <start id> <stop id> "
}

function args_parsing
{
	if [ -z "$CITY" ] || [ -z "$startID" ] || [ -z "$stopID" ]; then
		using
		exit
	fi
}

function get_general_stop
{
	local curl_message="$1"
	local stop=$(echo "$curl_message" | grep "stop_name" | sed "s/.*stop_name = \"//;s/\".*//")
	echo -n "$stop"
}

function get_second_stop
{
	local curl_message="$1"
	local stop=$(echo "$curl_message" | grep "fa-arrow-right" | sed -e "s/.*fa-arrow-right\"><\/i> //;s/<\/.*//")
	echo -n "$stop"
}

function get_gps
{
	local curl_message="$1"
	local x=$(echo "$curl_message" | grep US_CITY_POINT_X | sed "s/.*= //;s/;//")
	local y=$(echo "$curl_message" | grep US_CITY_POINT_Y | sed "s/.*= //;s/;//")
	echo -n "$x, $y"
}

function _delay
{
	local id=$1
	if [[ $((id % 10)) -eq 0 ]]; then
		sleep 1
	fi
}

function main
{
	args_parsing
	local id=$startID
	local max_id=$stopID
	while [[ ! $id -gt $max_id  ]]; do
		#echo "[id: $id]"
		id=$((id + 1))
		local curl_message=$(curl "$LINK/$CITY/stop/id/$id/" 2>/dev/null)
		local general_stop=$(get_general_stop "$curl_message")
		local second_stop=$(get_second_stop "$curl_message")
		local gps=$(get_gps "$curl_message")
	        if [[ -z "$general_stop" ]]; then
			continue
	        fi
		if [[ -z "$second_stop" ]]; then
			continue
		fi
		if [[ -z "$gps" ]]; then
			continue
		fi
		echo "insert into stop values ($id, '$general_stop', '$second_stop', $gps, 'bus', '$CITY')"
		_delay $id
	done

}

# run
main

