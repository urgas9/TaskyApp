from datetime import datetime


def get_field_param_value(array, param_names_array):
    current_array = array
    for param in param_names_array:
        if param not in current_array:
            return "?"
        current_array = current_array[param]

    return current_array


def get_bluetooth_num_devices(data):
    env = get_field_param_value(data, ["environment"])
    if env != "?":
        num = env["num_bluetooth_devices_nearby"]
        if num > 0 or num == 0 and env["bluetooth_turned_on"]:
            return num
        else:
            return "?"
    else:
        return "?"


def get_wifi_num_devices(data):
    env = get_field_param_value(data, ["environment"])
    if env != "?":
        num = env["num_wifi_devices_nearby"]
        if num > 0 or num == 0 and env["wifi_turned_on"]:
            return num
        else:
            return "?"
    else:
        return "?"


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
        count = "?"

    return count


def remove_duplicates_from_list(d):
    new_d = []
    for x in d:
        if x not in new_d:
            new_d.append(x)
    return new_d


def get_hour_of_sensing(data):
    t_started = get_field_param_value(data, ["t_started"])
    if t_started != "?":
        date_hour = datetime.fromtimestamp(float(t_started) / 1000).hour
    else:
        date_hour = "?"

    return date_hour


def get_day_of_sensing(data):
    t_started = get_field_param_value(data, ["t_started"])
    if t_started != "?":
        date_day = datetime.fromtimestamp(float(t_started) / 1000).weekday()
    else:
        date_day = "?"

    return date_day
