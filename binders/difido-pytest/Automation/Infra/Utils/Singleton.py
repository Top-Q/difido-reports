
class Singleton(type):
    _instances = {}

    def __call__(cls, *args, **kwargs):
        if cls not in cls._instances:
            cls._instances[cls] = super(Singleton, cls).__call__(*args, **kwargs)
        return cls._instances[cls]




 # _instance = None
    # def __new__(class_, *args, **kwargs):
    #     if not isinstance(class_._instance, class_):
    #         class_._instance = object.__new__(class_, *args, **kwargs)
    #         class_._instance.__initialized = False
    #     return class_._instance
    #
    # def __init__(self):
    #     if (self.__initialized): return
    #     self.__initialized = True

    # def __new__(cls, *dt, **mp):
    #    if not hasattr(cls, '_inst'):
    #        cls._inst = super(Singleton, cls).__new__(cls, dt, mp)
    #    else:
    #        def init_pass(self, *dt, **mp):
    #            pass
    #
    #        cls.__init__ = init_pass
    #
    #    return cls._inst
