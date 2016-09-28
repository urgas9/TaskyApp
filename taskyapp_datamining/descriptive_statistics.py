# Copyright (c) 2016, University of Ljubljana, Slovenia

# Gasper Urh, gu7668@student.uni-lj.si

# Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby
# granted, provided that the above copyright notice and this permission notice appear in all copies.
# THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING
# ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL,
# DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
# WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE
# OR PERFORMANCE OF THIS SOFTWARE.

# The script calculates descriptive statistics and draws graphs using matplotlib library

import matplotlib.pyplot as plt
import time
import numpy as np
import helper_functions as u
import helper_data_functions as ud
from pymongo import MongoClient
from datetime import datetime

# total counts
count_nonlabeled = 0
count_labeled = 0
count_duplicated = 0
count_all = 0
set_user_record_tuple = set()

# arrays
daily_nonlabeled = np.zeros(7)
daily_labeled = np.zeros(7)
hourly_nonlabeled = np.zeros(24)
hourly_labeled = np.zeros(24)

best_user_device_id = "d1a0319b57367267"
best_user_daily_nonlabeled = np.zeros(7)
best_user_daily_labeled = np.zeros(7)
best_user_hourly_nonlabeled = np.zeros(24)
best_user_hourly_labeled = np.zeros(24)

all_device_ids = set()

hourly_sum_of_labels = np.zeros(24)
best_user_hourly_sum_of_labels = np.zeros(24)

daily_sum_of_labels = np.zeros(7)
best_user_daily_sum_of_labels = np.zeros(7)

# per user array
users_labeled_count = {}
users_labeled_sum = {}

# per task array
task_per_label = np.zeros(5)

# Define colors
# primary_color = "#FFA000"
# secondary_color = "#FFECB3"
primary_color = "#0288D1"
secondary_color = "#B3E5FC"
tertiary_color = "#727272"
bar_border_color = "none"

best_user_primary_color = "#1976D2"
best_user_secondary_color = "#BBDEFB"


def calculate_average_per_user():
    global users_labeled_count
    global users_labeled_sum
    users_average = {}

    for device_id, sum in users_labeled_sum.items():
        users_average[device_id] = sum * 1.0 / users_labeled_count[device_id]

    mean = 0
    sum_all = 0
    for device_id, avg in users_average.items():
        mean += avg * users_labeled_count[device_id]
        sum_all += users_labeled_count[device_id]

    mean = mean / sum_all

    # print "Controlling average: " + str(mean)
    return users_average


def go_through_data_entries(fun, mongodb_collection):
    global count_duplicated
    global count_labeled
    global count_nonlabeled
    global count_all
    global users_labeled_count
    global users_labeled_sum
    global task_per_label

    for user in mongodb_collection:
        # print "\n -->USER_EMAIL: " + user["auth"]["email"]

        device_id = user["auth"]["device_id"]
        all_device_ids.add(device_id)
        labels_per_user = []
        for data in user["data"]:
            count_all += 1
            record_id = ud.get_field_param_value(data, ["database_id"])
            t_started = ud.get_field_param_value(data, ["t_started"])

            record_unique = (device_id, record_id, t_started)

            # There are some duplicate records, remove them
            if record_unique in set_user_record_tuple:
                count_duplicated += 1
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

            # DATA IS ALRIGHT, DO SOMETHING
            label = ud.get_field_param_value(data, ["label"])

            if label == "?" or label <= 0:
                count_nonlabeled += 1
            else:
                task_per_label[label - 1] += 1

                if device_id not in users_labeled_count:
                    users_labeled_count[device_id] = 0
                    users_labeled_sum[device_id] = 0
                users_labeled_count[device_id] += 1
                users_labeled_sum[device_id] += label
                count_labeled += 1

            fun(device_id, data)


def extract_statistics(device_id, data):
    global hourly_nonlabeled
    global hourly_labeled
    global daily_labeled
    global daily_nonlabeled
    global hourly_sum_of_labels
    global best_user_hourly_sum_of_labels

    global best_user_hourly_nonlabeled
    global best_user_daily_nonlabeled
    global best_user_hourly_labeled
    global best_user_daily_labeled

    label = ud.get_field_param_value(data, ["label"])
    hour_of_day = ud.get_hour_of_sensing(data)
    day_of_week = ud.get_day_of_sensing(data)

    if label == "?" or label <= 0:
        hourly_nonlabeled[hour_of_day] += 1
        daily_nonlabeled[day_of_week] += 1
        if device_id == best_user_device_id:
            best_user_hourly_nonlabeled[hour_of_day] += 1
            best_user_daily_nonlabeled[day_of_week] += 1
    else:
        hourly_labeled[hour_of_day] += 1
        hourly_sum_of_labels[hour_of_day] += label
        daily_labeled[day_of_week] += 1
        daily_sum_of_labels[day_of_week] += label
        if device_id == best_user_device_id:
            best_user_hourly_labeled[hour_of_day] += 1
            best_user_daily_labeled[day_of_week] += 1
            best_user_hourly_sum_of_labels[hour_of_day] += label
            best_user_daily_sum_of_labels[day_of_week] += label


def show_daily_plot(labeled_array, nonlabeled_array, best_user_labeled_array, best_user_nonlabeled_array):
    global primary_color
    global secondary_color
    global best_user_primary_color
    global best_user_secondary_color
    global bar_border_color

    N = 7
    width = 0.4
    ind = np.arange(N)
    p1 = plt.bar(ind + width / 2., labeled_array, width, color=primary_color, edgecolor=primary_color)
    p2 = plt.bar(ind + width / 2., nonlabeled_array, width, color=secondary_color, bottom=labeled_array,
                 edgecolor=bar_border_color)

    # u_p1 = plt.bar(ind+width, best_user_labeled_array, width, color=best_user_primary_color)
    # u_p2 = plt.bar(ind+width, best_user_nonlabeled_array, width, color=best_user_secondary_color, bottom=best_user_labeled_array)

    plt.xticks(ind + width, ('Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'))
    plt.yticks([10, 50, 90, 150, 220, 300, 400, 500, 600, 700])
    # plt.xaxis.grid(True)

    plt.ylabel("Number of tasks", fontweight='bold')
    plt.xlabel("Day of week", fontweight='bold')
    plt.legend((p1[0], p2[0]), ('Labeled', 'Non-labeled'))
    plt.show()


def show_hourly_plot(labeled_array, nonlabeled_array, best_user_labeled_array, best_user_nonlabeled_array):
    global primary_color
    global secondary_color
    global best_user_primary_color
    global best_user_secondary_color
    global bar_border_color
    N = 24
    width = 0.4
    ind = np.arange(N)
    p1 = plt.bar(ind + width / 2., labeled_array, width, color=primary_color, edgecolor=primary_color)
    p2 = plt.bar(ind + width / 2., nonlabeled_array, width, color=secondary_color, bottom=labeled_array,
                 edgecolor=bar_border_color)

    # b_p1 = plt.bar(ind+width, best_user_labeled_array, width, color=best_user_primary_color)
    # b_p2 = plt.bar(ind+width, best_user_nonlabeled_array, width, color=best_user_secondary_color, bottom=best_user_labeled_array)

    plt.xticks(ind + width, np.arange(N))
    plt.yticks(np.arange(0, 450, 25))
    # plt.xaxis.grid(True)

    plt.ylabel('Number of tasks', fontweight='bold')
    plt.xlabel('Hour of day', fontweight='bold')
    plt.legend((p1[0], p2[0]), ('Labeled', 'Non-labeled'))
    plt.show()


def show_hourly_average_label_plot(average_labels_array, mean, best_user_average_labels_array, best_user_mean):
    global primary_color
    global secondary_color
    global best_user_primary_color
    global best_user_secondary_color
    global tertiary_color
    global bar_border_color
    N = 24
    width = 0.4
    ind = np.arange(N)

    plt.axhline(mean, color=tertiary_color, linestyle='dashed', linewidth='1.3', zorder=5)
    # plt.axhline(best_user_mean, color=best_user_secondary_color, linestyle='dashed', linewidth='1.7', zorder=5)

    p1 = plt.bar(ind + width / 2., average_labels_array, width, color=primary_color, zorder=6,
                 edgecolor=bar_border_color)
    # p2 = plt.bar(ind + width, best_user_average_labels_array, width, color=best_user_primary_color, zorder=6)
    plt.xticks(ind + width, np.arange(N))
    # plt.yticks([1, 2, mean, best_user_mean, 3, 4, 5])
    plt.yticks([1, 2, mean, 3, 4, 5])

    plt.ylabel('Label', fontweight='bold')
    plt.xlabel('Hour of day', fontweight='bold')
    plt.show()


def show_daily_average_plot(average_labels_array, mean, best_user_average_labels_array, best_user_mean):
    global primary_color
    global secondary_color
    global tertiary_color
    global best_user_primary_color
    global best_user_secondary_color
    global bar_border_color

    N = 7
    width = 0.4
    ind = np.arange(N)

    plt.axhline(mean, color=tertiary_color, linestyle='dashed', linewidth='1.3', zorder=5)
    # plt.axhline(best_user_mean, color=best_user_secondary_color, linestyle='dashed', linewidth='1.7', zorder=5)

    p1 = plt.bar(ind + width / 2., average_labels_array, width, color=primary_color, zorder=6,
                 edgecolor=bar_border_color)
    # u_p1 = plt.bar(ind+width, best_user_average_labels_array, width, color=best_user_primary_color,  zorder=6)

    plt.xticks(ind + width, ('Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'))
    # plt.yticks([1, 2, mean, best_user_mean, 3, 4, 5])
    plt.yticks([1, 2, mean, 3, 4, 5])

    plt.ylabel('Label', fontweight='bold')
    plt.xlabel('Day of week', fontweight='bold')
    plt.show()


def show_piechart_per_task_distribution(per_task_distribution_array):
    colors = ["#64DD17", "#AEEA00", "#FFD600", "#FF6D00", "#DD2C00"]
    labels = ["Very easy", "Pretty easy", "Neither easy nor hard", "Pretty hard", "Very hard"]
    percentages = (per_task_distribution_array / sum(per_task_distribution_array)) * 100

    if sum(percentages) != 100:
        print "Drawing piechart failed. Something wrong with data, sum of percentages is not 100%."
        return

    # Create a pie chart
    pie_plot = plt.pie(
        # using data total)arrests
        per_task_distribution_array,
        # with the labels being officer names
        labels=labels,
        # with no shadows
        shadow=False,
        # with colors
        colors=colors,
        # with one slide exploded out
        explode=(0, 0, 0, 0, 0),
        # with the start angle at 90%
        startangle=0,
        # with the percent listed as a fraction
        autopct='%1.1f%%',
    )

    for pie_wedge in pie_plot[0]:
        pie_wedge.set_linewidth(2)
        pie_wedge.set_edgecolor('white')

    # View the plot drop above
    plt.axis('equal')

    # View the plot
    plt.tight_layout(2)
    plt.show()


# Running the code
client = MongoClient()
users_collection = client.taskyapp_sec.users.find()
users_nonlabeled_collection = client.taskyapp_sec.users_nonlabeled.find()
users_nonlabeled1_collection = client.taskyapp_sec.users_nonlabeled1.find()
users_nonlabeled2_collection = client.taskyapp_sec.users_nonlabeled2.find()
users_nonlabeled3_collection = client.taskyapp_sec.users_nonlabeled3.find()

first_release_date = datetime.strptime('02/04/2016 14:49:35', '%d/%m/%Y %H:%M:%S')
first_release_millis = int(time.mktime(first_release_date.timetuple()) * 1000)

print "--- DATA STATISTICS ---"
go_through_data_entries(extract_statistics, users_nonlabeled_collection)
go_through_data_entries(extract_statistics, users_nonlabeled1_collection)
go_through_data_entries(extract_statistics, users_nonlabeled2_collection)
go_through_data_entries(extract_statistics, users_nonlabeled3_collection)
go_through_data_entries(extract_statistics, users_collection)

show_piechart_per_task_distribution(task_per_label)

print "--- TOTAL --- "
print "NON_LABELED = " + str(count_nonlabeled)
print "LABELED = " + str(count_labeled)
print "DUPLICATED = " + str(count_duplicated)
print "ALL = " + str(count_all)
print "ALL_DEVICE_IDS = " + str(all_device_ids)

print "--- DAILY DATA ---"
print daily_labeled
print daily_nonlabeled

print "--- HOURLY DATA ---"
print hourly_labeled
print hourly_nonlabeled

print "MAX_DAILY_NON_LABELED: " + str(np.amax(daily_nonlabeled))
print "MIN_DAILY_NON_LABELED: " + str(np.amin(daily_nonlabeled))
print "MAX_HOURLY_NON_LABELED: " + str(np.amax(hourly_nonlabeled))
print "MIN_HOURLY_NON_LABELED: " + str(np.amin(hourly_nonlabeled))

print "MAX_DAILY_LABELED: " + str(np.amax(daily_labeled))
print "MIN_DAILY_LABELED: " + str(np.amin(daily_labeled))
print "MAX_HOURLY_LABELED: " + str(np.amax(hourly_labeled))
print "MIN_HOURLY_LABELED: " + str(np.amin(hourly_labeled))

print "--- PER USER ---"
print users_labeled_count
print users_labeled_sum
average_difficulty_per_user = calculate_average_per_user()
print average_difficulty_per_user

show_daily_plot(daily_labeled, daily_nonlabeled, best_user_daily_labeled, best_user_daily_nonlabeled)
show_hourly_plot(hourly_labeled, hourly_nonlabeled, best_user_hourly_labeled, best_user_hourly_nonlabeled)

# print hourly_labeled
# print hourly_sum_of_labels

mean_label = (sum(hourly_sum_of_labels) * 1.0 / sum(hourly_labeled))
print "MEAN LABEL: " + str(mean_label)
best_user_mean_label = (sum(best_user_hourly_sum_of_labels) * 1.0 / sum(best_user_hourly_labeled))
print "BEST_USER_MEAN LABEL: " + str(best_user_mean_label)

print "--- HOURLY DISTRIBUTION ---"
print "Num. of labels hourly: " + str(hourly_labeled)
print "Num. of non-labeled hourly: " + str(hourly_nonlabeled)

print "--- DAILY DISTRIBUTION ---"
print "Num. of non-labeled daily: " + str(daily_nonlabeled)
print "Num. of labels daily: " + str(daily_labeled)
print "Daily sum of labels: " + str(daily_sum_of_labels)

hourly_labeled = [x if x > 1 else 0 for x in hourly_labeled]
# best_user_hourly_labeled = [x if x > 2 else 0 for x in best_user_hourly_labeled]

average_label_per_hour = u.divide_two_arrays(hourly_sum_of_labels, hourly_labeled)
best_user_average_label_per_hour = u.divide_two_arrays(best_user_hourly_sum_of_labels, best_user_hourly_labeled)

show_hourly_average_label_plot(average_label_per_hour, mean_label, best_user_average_label_per_hour,
                               best_user_mean_label)

print "--- AVERAGE LABELS DAILY ---"
mean_daily_label = (sum(daily_sum_of_labels) * 1.0 / sum(daily_labeled))
print "Mean daily label: " + str(mean_label)

best_user_mean_daily_label = (sum(best_user_daily_sum_of_labels) * 1.0 / sum(best_user_daily_labeled))
print "Best user's mean daily label: " + str(best_user_mean_daily_label)

daily_labeled = [x if x > 1 else 0 for x in daily_labeled]
average_labels_per_day = u.divide_two_arrays(daily_sum_of_labels, daily_labeled)
best_user_average_labels_per_day = u.divide_two_arrays(best_user_daily_sum_of_labels, best_user_daily_labeled)

show_daily_average_plot(average_labels_per_day, mean_daily_label, best_user_average_labels_per_day,
                        best_user_mean_daily_label)
