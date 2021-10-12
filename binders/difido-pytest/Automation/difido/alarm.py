#!/usr/bin/python

# lightly modified version of http://code.activestate.com/recipes/577600-queue-for-managing-multiple-sigalrm-alarms-concurr/


"""alarm.py: Permits multiple SIGALRM events to be queued.

Uses a `heapq` to store the objects to be called when an alarm signal is
raised, so that the next alarm is always at the top of the heap.
"""

import heapq
import signal
from time import time

__version__ = '$Revision: 2539 $'.split()[1]

alarmlist = []

__new_alarm = lambda t, f, a, k: (t + time(), f, a, k)
__next_alarm = lambda: int(round(alarmlist[0][0] - time())) if alarmlist else None
__set_alarm = lambda: signal.alarm(max(__next_alarm(), 1))


class TimeoutError(Exception):
    def __init__(self, message, id_=None):
        self.message = message
        self.id_ = id_


class Timeout:
    ''' id_ allows for nested timeouts. '''
    def __init__(self, id_=None, seconds=1, error_message='Timeout'):
        self.seconds = seconds
        self.error_message = error_message
        self.id_ = id_
    def handle_timeout(self):
        raise TimeoutError(self.error_message, self.id_)
    def __enter__(self):
        self.this_alarm = alarm(self.seconds, self.handle_timeout)
    def __exit__(self, type, value, traceback):
        try:
            cancel(self.this_alarm)
        except ValueError:
            pass


def __clear_alarm():
    """Clear an existing alarm.

    If the alarm signal was set to a callable other than our own, queue the
    previous alarm settings.
    """
    oldsec = signal.alarm(0)
    oldfunc = signal.signal(signal.SIGALRM, __alarm_handler)
    if oldsec > 0 and oldfunc != __alarm_handler:
        heapq.heappush(alarmlist, (__new_alarm(oldsec, oldfunc, [], {})))


def __alarm_handler(*zargs):
    """Handle an alarm by calling any due heap entries and resetting the alarm.

    Note that multiple heap entries might get called, especially if calling an
    entry takes a lot of time.
    """
    try:
        nextt = __next_alarm()
        while nextt is not None and nextt <= 0:
            (tm, func, args, keys) = heapq.heappop(alarmlist)
            func(*args, **keys)
            nextt = __next_alarm()
    finally:
        if alarmlist: __set_alarm()


def alarm(sec, func, *args, **keys):
    """Set an alarm.

    When the alarm is raised in `sec` seconds, the handler will call `func`,
    passing `args` and `keys`. Return the heap entry (which is just a big
    tuple), so that it can be cancelled by calling `cancel()`.
    """
    __clear_alarm()
    try:
        newalarm = __new_alarm(sec, func, args, keys)
        heapq.heappush(alarmlist, newalarm)
        return newalarm
    finally:
        __set_alarm()


def cancel(alarm):
    """Cancel an alarm by passing the heap entry returned by `alarm()`.

    It is an error to try to cancel an alarm which has already occurred.
    """
    __clear_alarm()
    try:
        alarmlist.remove(alarm)
        heapq.heapify(alarmlist)
    finally:
        if alarmlist: __set_alarm()