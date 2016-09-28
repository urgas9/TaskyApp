# Copyright (c) 2016, University of Ljubljana, Slovenia

# Gasper Urh, gu7668@student.uni-lj.si

# This project was developed as part of the paper submitted for the UbitTention workshop (in conjunction with
# UbiComp'16) and my master thesis. For more information, please visit http://projects.hcilab.org/ubittention/

# Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby
# granted, provided that the above copyright notice and this permission notice appear in all copies.
# THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING
# ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL,
# DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
# WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE
# OR PERFORMANCE OF THIS SOFTWARE.

# This helper script is used to help us extract features out of an array objects after querying MongoDB

from datetime import datetime

default_missing_numeric = "?"


def get_field_param_value(array, param_names_array):
    current_array = array
    for param in param_names_array:
        if param not in current_array:
            return "?"
        current_array = current_array[param]

    return current_array


def get_bluetooth_num_devices(data):
    global default_missing_numeric

    env = get_field_param_value(data, ["environment"])
    if env != "?":
        num = env["num_bluetooth_devices_nearby"]
        if num > 0 or num == 0 and env["bluetooth_turned_on"]:
            return num
        else:
            return default_missing_numeric
    else:
        return default_missing_numeric


def get_wifi_num_devices(data):
    global default_missing_numeric

    env = get_field_param_value(data, ["environment"])
    if env != "?":
        num = env["num_wifi_devices_nearby"]
        if num > 0 or num == 0 and env["wifi_turned_on"]:
            return num
        else:
            return default_missing_numeric
    else:
        return default_missing_numeric


def get_screen_status(data):
    arr = get_field_param_value(data, ["screen_status_list"])
    if arr != "?" and len(arr) > 0:
        screen_on = get_field_param_value(arr[0], ["screen_on"])
        if screen_on != "?":
            if screen_on:
                return "on"
            else:
                return "off"
    return "?"


def get_num_calendar_events(data):
    global default_missing_numeric

    app_ver = get_field_param_value(data, ["app_version"])
    if app_ver == "?":
        print "WRONG APP VERSION, WTF?!"
    # Calendar events were included in app version 10, maybe 9
    calendars_array = get_field_param_value(data, ["active_calendar_events"])
    if calendars_array != "?":
        # Remove duplicates
        # calendars_set_array = [dict(tupleized) for tupleized in set(tuple(item.items()) for item in calendars_array)]
        calendars_set_array = remove_duplicates_from_list(calendars_array)
        count = len(calendars_set_array)
    elif app_ver != "?" and app_ver > 9:
        count = 0
    else:
        count = default_missing_numeric

    return count


def remove_duplicates_from_list(d):
    new_d = []
    for x in d:
        if x not in new_d:
            new_d.append(x)
    return new_d


def get_hour_of_sensing(data):
    global default_missing_numeric

    t_started = get_field_param_value(data, ["t_started"])
    if t_started != "?":
        date_hour = datetime.fromtimestamp(float(t_started) / 1000).hour
    else:
        date_hour = default_missing_numeric

    return date_hour


def get_day_of_sensing(data):
    t_started = get_field_param_value(data, ["t_started"])
    if t_started != "?":
        date_day = datetime.fromtimestamp(float(t_started) / 1000).weekday()
    else:
        date_day = "?"

    return date_day
