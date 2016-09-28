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

# This script is used to extract features from collected data stored in our MongoDB

from datetime import datetime

import arff
import time
import numpy as np
import helper_functions as u
import helper_data_functions as ud
from pymongo import MongoClient

# Start database: mongod.exe --dbpath "d:\MongoDb\data"
# About data mining https://www.ibm.com/developerworks/library/os-weka1/
# Classification and clustering: http://www.ibm.com/developerworks/opensource/library/os-weka2/index.html
# Closest neighbour and Java API: http://www.ibm.com/developerworks/opensource/library/os-weka3/index.html

client = MongoClient()
users_collection = client.taskyapp_sec.users

all_users = users_collection.find()

arff_dataset = {
    'relation': 'users',
    'description': 'Users Dataset',
    'attributes': [
        ('label_nominal', ['easy', 'difficult']),
        ('label_numeric', 'NUMERIC'),
        # ('sensing_policy', ['NONE', 'LOCATION_UPDATES', 'INTERVAL', 'ACTIVITY_UPDATES', 'USER_FORCED']),
        ('num_wifi_devices', 'NUMERIC'),
        ('num_bluetooth_devices', 'NUMERIC'),
        ('accelerometer_mean_x', 'NUMERIC'),
        ('accelerometer_mean_y', 'NUMERIC'),
        ('accelerometer_mean_z', 'NUMERIC'),
        ('accelerometer_mean_fft_x', 'NUMERIC'),
        ('accelerometer_mean_fft_y', 'NUMERIC'),
        ('accelerometer_mean_fft_z', 'NUMERIC'),

        ('accelerometer_spec_entropy_x', 'NUMERIC'),
        ('accelerometer_spec_entropy_y', 'NUMERIC'),
        ('accelerometer_spec_entropy_z', 'NUMERIC'),
        ('accelerometer_mean_intensity', 'NUMERIC'),
        ('accelerometer_mean_intensity_crossing_rate', 'NUMERIC'),
        ('accelerometer_intensity_variance', 'NUMERIC'),
        # ('accelerometer_mean_crossings_x', 'NUMERIC'),
        # ('accelerometer_mean_crossings_y', 'NUMERIC'),
        # ('accelerometer_mean_crossings_z', 'NUMERIC'),
        ('gyroscope_mean_x', 'NUMERIC'),
        ('gyroscope_mean_y', 'NUMERIC'),
        ('gyroscope_mean_z', 'NUMERIC'),
        ('gyroscope_mean_fft_x', 'NUMERIC'),
        ('gyroscope_mean_fft_y', 'NUMERIC'),
        ('gyroscope_mean_fft_z', 'NUMERIC'),
        ('gyroscope_spec_entropy_x', 'NUMERIC'),
        ('gyroscope_spec_entropy_y', 'NUMERIC'),
        ('gyroscope_spec_entropy_z', 'NUMERIC'),
        ('gyroscope_mean_intensity', 'NUMERIC'),
        ('gyroscope_mean_intensity_crossing_rate', 'NUMERIC'),
        ('gyroscope_intensity_variance', 'NUMERIC'),
        # ('gyroscope_mean_crossings_x', 'NUMERIC'),
        # ('gyroscope_mean_crossings_y', 'NUMERIC'),
        # ('gyroscope_mean_crossings_z', 'NUMERIC'),
        ('microphone_mean', 'NUMERIC'),
        ('microphone_mean_crossing', 'NUMERIC'),
        ('microphone_mean_fft', 'NUMERIC'),
        ('microphone_spec_entropy', 'NUMERIC'),
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
        ('hour_of_day', 'NUMERIC')
        # ('t_started_pretty', 'STRING')
    ]
}

attribute_names = []
for attr in arff_dataset["attributes"]:
    attribute_names.append(attr[0])

first_release_date = datetime.strptime('02/04/2016 14:49:35', '%d/%m/%Y %H:%M:%S')
first_release_millis = int(time.mktime(first_release_date.timetuple()) * 1000)

best_user_device_id = "d1a0319b57367267"
second_best_device_id = "b5a838db2b5ce0db"
third_best_device_id = "2a0083ebe04d60a1"

best_user_device_id = best_user_device_id

arff_data = []
arff_data_best_user = []
set_user_record_tuple = set()
count_all = 0
count_used = 0
count_before_release = 0

number_of_user_labels = []
all_labels = []

print "--- USERS ---"
users_count = 0
for user in all_users:
    # print "\n -->USER_EMAIL: " + user["auth"]["email"]

    device_id = user["auth"]["device_id"]
    labels_per_user = []
    for data in user["data"]:
        count_all += 1
        record_id = ud.get_field_param_value(data, ["database_id"])
        t_started = ud.get_field_param_value(data, ["t_started"])
        record_unique = (device_id, t_started, record_id)

        # There are some duplicate records, remove them
        if record_unique in set_user_record_tuple:
            continue
        else:
            set_user_record_tuple.add(record_unique)

        # App was tested before official release was made, check for those
        if t_started != "?":
            t_started = long(t_started)
            if t_started < first_release_millis:
                count_before_release += 1
                continue
        else:
            continue

        # print str(t_started) + " vs " + str(first_release_millis)

        arff_line_data = []

        task_label = ud.get_field_param_value(data, ["label"])

        # Append nominal label first ('difficult', 'easy')
        nominal_label = task_label
        if nominal_label != "?":
            nominal_label = 'difficult' if nominal_label >= 3 else 'easy'
        arff_line_data.append(nominal_label)
        # Append numeric task label 1-5
        arff_line_data.append(task_label)

        # arff_line_data.append(ud.get_field_param_value(data, ["sensing_policy"]))

        arff_line_data.append(ud.get_wifi_num_devices(data))
        arff_line_data.append(ud.get_bluetooth_num_devices(data))

        # Start accelerometer values
        acclValues = ud.get_field_param_value(data, ["accelerometer", "values"])
        if acclValues == "?":
            a_mean_calculated_accl_arrray = ["?", "?", "?"]
            a_mean_intensity = "?"
            a_mean_intensity_crossing_rate = "?"
            a_intensity_var = "?"
            a_mean_fft_x = "?"
            a_mean_fft_y = "?"
            a_mean_fft_z = "?"
            a_spec_entropy_x = "?"
            a_spec_entropy_y = "?"
            a_spec_entropy_z = "?"
            # a_crossing_rate_x = "?"
            # a_crossing_rate_y = "?"
            # a_crossing_rate_z = "?"
        else:
            a_mean_calculated_accl_arrray = np.mean(acclValues, axis=0, dtype=np.float64)
            a_np_array = np.array(acclValues)
            a_np_array_list = a_np_array.T.tolist()
            a_intensity_arr = u.get_intensity_data(a_np_array_list)
            a_mean_intensity = a_intensity_arr[0]
            a_mean_intensity_crossing_rate = a_intensity_arr[1]
            a_intensity_var = a_intensity_arr[2]
            a_mean_fft_x = u.mean_fft(acclValues[0])
            a_mean_fft_y = u.mean_fft(acclValues[1])
            a_mean_fft_z = u.mean_fft(acclValues[2])
            a_spec_entropy_x = u.spectral_entropy(acclValues[0])
            a_spec_entropy_y = u.spectral_entropy(acclValues[1])
            a_spec_entropy_z = u.spectral_entropy(acclValues[2])
            # a_crossing_rate_x = u.get_mean_crossings_rate(a_np_array_list[0])
            # a_crossing_rate_y = u.get_mean_crossings_rate(a_np_array_list[1])
            # a_crossing_rate_z = u.get_mean_crossings_rate(a_np_array_list[2])

        arff_line_data.append(a_mean_calculated_accl_arrray[0])
        arff_line_data.append(a_mean_calculated_accl_arrray[1])
        arff_line_data.append(a_mean_calculated_accl_arrray[2])

        arff_line_data.append(a_mean_fft_x)
        arff_line_data.append(a_mean_fft_y)
        arff_line_data.append(a_mean_fft_z)

        arff_line_data.append(a_spec_entropy_x)
        arff_line_data.append(a_spec_entropy_y)
        arff_line_data.append(a_spec_entropy_z)

        arff_line_data.append(a_mean_intensity)
        arff_line_data.append(a_mean_intensity_crossing_rate)
        arff_line_data.append(a_intensity_var)

        # arff_line_data.append(a_crossing_rate_x)
        # arff_line_data.append(a_crossing_rate_y)
        # arff_line_data.append(a_crossing_rate_z)

        # GYROSCOPE VALUES
        gyroValues = ud.get_field_param_value(data, ["gyroscope", "values"])
        if acclValues == "?":
            g_mean_calculated_accl_arrray = ["?", "?", "?"]
            g_mean_intensity = "?"
            g_mean_intensity_crossing_rate = "?"
            g_intensity_var = "?"
            g_mean_fft_x = "?"
            g_mean_fft_y = "?"
            g_mean_fft_z = "?"
            g_spec_entropy_x = "?"
            g_spec_entropy_y = "?"
            g_spec_entropy_z = "?"
            # g_crossing_rate_x = "?"
            # g_crossing_rate_y = "?"
            # g_crossing_rate_z = "?"
        else:
            g_mean_calculated_accl_arrray = np.mean(gyroValues, axis=0, dtype=np.float64)
            g_np_array = np.array(gyroValues)
            g_np_array_list = g_np_array.T.tolist()
            g_intensity_arr = u.get_intensity_data(g_np_array_list)
            g_mean_intensity = g_intensity_arr[0]
            g_mean_intensity_crossing_rate = g_intensity_arr[1]
            g_intensity_var = g_intensity_arr[2]
            g_mean_fft_x = u.mean_fft(gyroValues[0])
            g_mean_fft_y = u.mean_fft(gyroValues[1])
            g_mean_fft_z = u.mean_fft(gyroValues[2])
            g_spec_entropy_x = u.spectral_entropy(gyroValues[0])
            g_spec_entropy_y = u.spectral_entropy(gyroValues[1])
            g_spec_entropy_z = u.spectral_entropy(gyroValues[2])
            # g_crossing_rate_x = u.get_mean_crossings_rate(g_np_array_list[0])
            # g_crossing_rate_y = u.get_mean_crossings_rate(g_np_array_list[1])
            # g_crossing_rate_z = u.get_mean_crossings_rate(g_np_array_list[2])

        arff_line_data.append(g_mean_calculated_accl_arrray[0])
        arff_line_data.append(g_mean_calculated_accl_arrray[1])
        arff_line_data.append(g_mean_calculated_accl_arrray[2])

        arff_line_data.append(g_mean_fft_x)
        arff_line_data.append(g_mean_fft_y)
        arff_line_data.append(g_mean_fft_z)

        arff_line_data.append(g_spec_entropy_x)
        arff_line_data.append(g_spec_entropy_y)
        arff_line_data.append(g_spec_entropy_z)

        arff_line_data.append(g_mean_intensity)
        arff_line_data.append(g_mean_intensity_crossing_rate)
        arff_line_data.append(g_intensity_var)

        # arff_line_data.append(g_crossing_rate_x)
        # arff_line_data.append(g_crossing_rate_y)
        # arff_line_data.append(g_crossing_rate_z)

        # MICROPHONE
        mic_values = ud.get_field_param_value(data, ["microphone", "amplitudes"])
        if mic_values == "?":
            mean_calculated_mic = "?"
            mean_crossings_mic = "?"
            mean_fft_mic = "?"
            spec_entropy_mic = "?"
        else:
            mean_calculated_mic = int(np.mean(mic_values))
            mean_crossings_mic = u.get_mean_crossings_rate(mic_values)
            mean_fft_mic = u.mean_fft(mic_values)
            spec_entropy_mic = u.spectral_entropy(mic_values)

        arff_line_data.append(mean_calculated_mic)
        arff_line_data.append(mean_crossings_mic)
        arff_line_data.append(mean_fft_mic)
        arff_line_data.append(spec_entropy_mic)

        arff_line_data.append(ud.get_field_param_value(data, ["activity", "type"]))

        environment_data = ud.get_field_param_value(data, ["environment"])
        if environment_data != "?":
            charging = "no"
            if environment_data["battery_charging"]:
                charging = "yes"
            else:
                charging = "no"

            arff_line_data.append(charging)
        else:
            arff_line_data.append("?")

        arff_line_data.append(ud.get_screen_status(data))

        arff_line_data.append(ud.get_num_calendar_events(data))

        arff_line_data.append(ud.get_hour_of_sensing(data))

        # arff_line_data.append(ud.get_field_param_value(data, ["t_started_pretty"]))
        count_used += 1
        arff_data.append(arff_line_data)
        if device_id == best_user_device_id:
            arff_data_best_user.append(arff_line_data)

        labels_per_user.append(task_label)
        all_labels.append(task_label)

    users_count += 1
    print "- User" + str(users_count) + ": -"
    print "device_id: " + device_id
    print "Average label: " + "%.2f" % np.mean(labels_per_user)
    print "Num labels: " + str(len(labels_per_user))

print ""
print "--- STATISTICS ---"
print "Records used: " + str(count_used)
print "All records: " + str(count_all)
print "Records before release: " + str(count_before_release)
print "All labels: " + str(len(all_labels))
print "Average label: " + "%.2f" % np.mean(all_labels)
print "--- ---"
print ""

arff_dataset_best_user = arff_dataset.copy()

arff_dataset["data"] = arff_data
f = open("arff_taskyapp_data_232.arff", 'wb')
arff.dump(arff_dataset, f)
f.close()

arff_dataset_best_user["data"] = arff_data_best_user
f1 = open("arff_taskyapp_data_232_best_user.arff", 'wb')
arff.dump(arff_dataset_best_user, f1)
f1.close()
