# Copyright (c) 2016, University of Ljubljana, Slovenia

# Gasper Urh, gu7668@student.uni-lj.si

# Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby
# granted, provided that the above copyright notice and this permission notice appear in all copies.
# THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING
# ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL,
# DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
# WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE
# OR PERFORMANCE OF THIS SOFTWARE.

# The script handles a random draw for picking a random participant out of our top 5 data label providers

import operator
import time
import helper_data_functions as ud
from pymongo import MongoClient
from datetime import datetime
from random import randint

client = MongoClient()
users_collection = client.taskyapp_sec.users

all_users = users_collection.find()

user_device_info = {}
user_labels = {}

set_user_record_tuple = set()

first_release_date = datetime.strptime('02/04/2016 14:49:35', '%d/%m/%Y %H:%M:%S')
first_release_millis = int(time.mktime(first_release_date.timetuple()) * 1000)

for user in all_users:
    email = user["auth"]["email"]
    device_id = user["auth"]["device_id"]
    user_device_info[device_id] = email

    for data in user["data"]:
        record_id = ud.get_field_param_value(data, ["database_id"])
        t_started = ud.get_field_param_value(data, ["t_started"])

        record_unique = (device_id, record_id, t_started)

        # There are some duplicate records, remove them
        if record_unique in set_user_record_tuple:
            continue
        else:
            set_user_record_tuple.add(record_unique)

        # App was tested before official release was made, check for those
        if t_started != "?":
            t_started = long(t_started)
            if t_started < first_release_millis:
                # print "before official release"
                continue
        else:
            # print "t_started not provided"
            continue

        # Data is alright
        if device_id not in user_labels:
            user_labels[device_id] = 0
        user_labels[device_id] += 1

# print "ALL DEVICES: "
# print user_device_info
# print "LABELS PROVIDED: "
# print user_labels
# print "EMAILS BY COMMA: "
# print ','.join(user_device_info.itervalues())

# Sort
sorted_users = sorted(user_labels.items(), key=operator.itemgetter(1), reverse=True)
# Picking randomly one of the top 5 providers
rand = randint(0, 4)
print "----------------"
print "THE WINNER IS:"
winner_device_id = sorted_users[rand][0]
print "device_id = " + winner_device_id
print "email = " + user_device_info[winner_device_id]
print "collected labels = " + str(user_labels[winner_device_id])
print "----------------"

# At the time of the draw the winner was:
# device_id = b5a838db2b5ce0db
# email = magres@email.si
# collected labels = 57
