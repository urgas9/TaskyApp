from datetime import datetime

import arff
import time
import numpy as np
import helper_functions as u
import helper_data_functions as ud
from pymongo import MongoClient

# Start database: mongod.exe --dbpath "d:\MongoDb\data"

client = MongoClient()
users_collection = client.taskyapp.users

all_users = users_collection.find()

arff_dataset = {
    'relation': 'users',
    'description': 'Users Dataset',
    'attributes': [
        ('label', 'NUMERIC'),
        ('sensing_policy', ['NONE', 'LOCATION_UPDATES', 'INTERVAL', 'ACTIVITY_UPDATES', 'USER_FORCED']),
        ('num_wifi_devices', 'NUMERIC'),
        ('num_bluetooth_devices', 'NUMERIC'),
        ('accelerometer_mean_x', 'NUMERIC'),
        ('accelerometer_mean_y', 'NUMERIC'),
        ('accelerometer_mean_z', 'NUMERIC'),
        ('accelerometer_mean_intensity', 'NUMERIC'),
        ('accelerometer_intensity_variance', 'NUMERIC'),
        ('accelerometer_mean_crossings_x', 'NUMERIC'),
        ('accelerometer_mean_crossings_y', 'NUMERIC'),
        ('accelerometer_mean_crossings_z', 'NUMERIC'),
        ('gyroscope_mean_x', 'NUMERIC'),
        ('gyroscope_mean_y', 'NUMERIC'),
        ('gyroscope_mean_z', 'NUMERIC'),
        ('gyroscope_mean_intensity', 'NUMERIC'),
        ('gyroscope_intensity_variance', 'NUMERIC'),
        ('gyroscope_mean_crossings_x', 'NUMERIC'),
        ('gyroscope_mean_crossings_y', 'NUMERIC'),
        ('gyroscope_mean_crossings_z', 'NUMERIC'),
        ('microphone_mean', 'NUMERIC'),
        ('microphone_mean_crossing', 'NUMERIC'),
        ('google_activity', [
            'In Vehicle',
            'On Bicycle',
            'On Foot',
            'Walking',
            'Still',
            'Tilting',
            'Running',
            'Unknown'
        ]),
        ('charging', ['yes', 'no']),
        ('screen_on', ['on', 'off']),
        ('num_of_calendar_events', 'NUMERIC'),
        ('hour_of_day', 'NUMERIC'),
        ('t_started_pretty', 'STRING')
    ]
}

first_release_date = datetime.strptime('02/04/2016 14:49:35', '%d/%m/%Y %H:%M:%S')
print "MILLIS " + str(first_release_date.microsecond)
print first_release_date.strftime("%Y-%m-%d %H:%M:%S")
first_release_millis = int(time.mktime(first_release_date.timetuple()) * 1000)
print first_release_millis

aarf_data = []
set_user_record_tuple = set()
count_all = 0
count_used = 0
count_before_release = 0
for user in all_users:
    print "USER: " + user["auth"]["email"]

    device_id = user["auth"]["device_id"]
    for data in user["data"]:
        count_all += 1
        record_id = ud.get_field_param_value(data, ["database_id"])
        record_unique = (device_id, record_id)

        # There are some duplicate records, remove them
        if record_unique in set_user_record_tuple:
            continue
        else:
            set_user_record_tuple.add(record_unique)

        # App was tested before official release was made, check for those
        t_started = ud.get_field_param_value(data, ["t_started"])
        if t_started != "?":
            t_started = long(t_started)
            if t_started < first_release_millis:
                count_before_release += 1

        print str(t_started) + " vs " + str(first_release_millis)

        aarf_line_data = []

        aarf_line_data.append(ud.get_field_param_value(data, ["label"]))
        aarf_line_data.append(ud.get_field_param_value(data, ["sensing_policy"]))

        aarf_line_data.append(ud.get_wifi_num_devices(data))
        aarf_line_data.append(ud.get_bluetooth_num_devices(data))

        # Start accelerometer values
        acclValues = ud.get_field_param_value(data, ["accelerometer", "values"])
        if acclValues == "?":
            a_mean_calculated_accl_arrray = ["?", "?", "?"]
            a_mean_intensity = "?"
            a_intensity_var = "?"
            a_crossing_rate_x = "?"
            a_crossing_rate_y = "?"
            a_crossing_rate_z = "?"
        else:
            a_mean_calculated_accl_arrray = np.mean(acclValues, axis=0, dtype=np.float64)
            a_np_array = np.array(acclValues)
            a_np_array_list = a_np_array.T.tolist()
            a_intensity_arr = u.get_mean_intensity(a_np_array_list)
            a_mean_intensity = a_intensity_arr[0]
            a_intensity_var = a_intensity_arr[1]
            a_crossing_rate_x = u.get_mean_crossings_rate(a_np_array_list[0])
            a_crossing_rate_y = u.get_mean_crossings_rate(a_np_array_list[1])
            a_crossing_rate_z = u.get_mean_crossings_rate(a_np_array_list[2])

        aarf_line_data.append(a_mean_calculated_accl_arrray[0])
        aarf_line_data.append(a_mean_calculated_accl_arrray[1])
        aarf_line_data.append(a_mean_calculated_accl_arrray[2])

        aarf_line_data.append(a_mean_intensity)
        aarf_line_data.append(a_intensity_var)

        aarf_line_data.append(a_crossing_rate_x)
        aarf_line_data.append(a_crossing_rate_y)
        aarf_line_data.append(a_crossing_rate_z)

        # GYROSCOPE VALUES
        gyroValues = ud.get_field_param_value(data, ["gyroscope", "values"])
        if acclValues == "?":
            g_mean_calculated_accl_arrray = ["?", "?", "?"]
            g_mean_intensity = "?"
            g_intensity_var = "?"
            g_crossing_rate_x = "?"
            g_crossing_rate_y = "?"
            g_crossing_rate_z = "?"
        else:
            g_mean_calculated_accl_arrray = np.mean(gyroValues, axis=0, dtype=np.float64)
            g_np_array = np.array(gyroValues)
            g_np_array_list = g_np_array.T.tolist()
            g_intensity_arr = u.get_mean_intensity(g_np_array_list)
            g_mean_intensity = g_intensity_arr[0]
            g_intensity_var = g_intensity_arr[1]
            g_crossing_rate_x = u.get_mean_crossings_rate(g_np_array_list[0])
            g_crossing_rate_y = u.get_mean_crossings_rate(g_np_array_list[1])
            g_crossing_rate_z = u.get_mean_crossings_rate(g_np_array_list[2])

        aarf_line_data.append(g_mean_calculated_accl_arrray[0])
        aarf_line_data.append(g_mean_calculated_accl_arrray[1])
        aarf_line_data.append(g_mean_calculated_accl_arrray[2])

        aarf_line_data.append(g_mean_intensity)
        aarf_line_data.append(g_intensity_var)

        aarf_line_data.append(g_crossing_rate_x)
        aarf_line_data.append(g_crossing_rate_y)
        aarf_line_data.append(g_crossing_rate_z)

        # MICROPHONE
        mic_values = ud.get_field_param_value(data, ["microphone", "amplitudes"])
        if mic_values == "?":
            mean_calculated_mic = "?"
            mean_crossings_mic = "?"
        else:
            mean_calculated_mic = int(np.mean(mic_values))
            mean_crossings_mic = u.get_mean_crossings_rate(mic_values)

        aarf_line_data.append(mean_calculated_mic)
        aarf_line_data.append(mean_crossings_mic)

        aarf_line_data.append(ud.get_field_param_value(data, ["activity", "type"]))

        environment_data = ud.get_field_param_value(data, ["environment"])
        if environment_data != "?":
            charging = "no"
            if environment_data["battery_charging"]:
                charging = "yes"
            else:
                charging = "no"

            aarf_line_data.append(charging)
        else:
            aarf_line_data.append("?")

        aarf_line_data.append(ud.get_screen_status(data))

        aarf_line_data.append(ud.get_num_calendar_events(data))

        aarf_line_data.append(ud.get_hour_of_sensing(data))
        aarf_line_data.append(ud.get_field_param_value(data, ["t_started_pretty"]))
        print aarf_line_data
        count_used += 1
        aarf_data.append(aarf_line_data)

print ""
print "--- STATISTICS ---"
print "Records used: " + str(count_used)
print "All records: " + str(count_all)
print "Records before release: " + str(count_before_release)
print ""

arff_dataset["data"] = aarf_data

f = open("aarf_taskyapp_data_n.arff", 'wb')
arff.dump(arff_dataset, f)
f.close()
