import numpy as np


# Returns an array with values of [mean_intensity, intensity_crossing_rate, variance]
def get_intensity_data(three_dim_array):
    if len(three_dim_array) != 3:
        print "THIS IS NOT A 3-dim ARRAY!"
    if len(three_dim_array[0]) != len(three_dim_array[1]) or len(three_dim_array[0]) != len(three_dim_array[2]):
        print "SIZES OF ARRAYS ARE NOT SAME!"

    arr_squared = np.power(three_dim_array, 2, dtype=np.float64)
    arr_sum1 = np.sum(arr_squared, axis=0, dtype=np.float64)
    arr_sqrt = np.sqrt(arr_sum1, dtype=np.float64)
    variance = np.var(arr_sqrt)
    mean_intensity = np.mean(arr_sqrt, dtype=np.float64)
    mean_intensity_crossing_rate = get_mean_crossings_rate(arr_sqrt)
    return [mean_intensity, mean_intensity_crossing_rate, variance]


def get_mean_crossings_rate(array):
    mean = np.mean(array, dtype=np.float64)
    new_zero_array = np.subtract(array, mean)
    return get_zero_crossings_rate(new_zero_array)


def get_zero_crossings_rate(array):
    return len(np.where(np.diff(np.signbit(array)))[0])


def divide_two_arrays(array1, array2):
    return [(x * 1.0) / y if y > 0 else 0 for x, y in zip(array1, array2)]


def mean_fft(array):
    X = np.fft.fft(array)
    return np.mean(np.abs(X)**2)

