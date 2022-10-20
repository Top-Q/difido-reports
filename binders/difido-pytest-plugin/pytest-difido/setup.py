#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import codecs
from setuptools import setup


def read(fname):
    file_path = os.path.join(os.path.dirname(__file__), fname)
    return codecs.open(file_path, encoding='utf-8').read()


setup(
    name='pytest-difido',
    version='0.5.0',
    author='Itai Agmon',
    author_email='itai.agmon@gmail.com',
    maintainer='Itai Agmon',
    maintainer_email='itai.agmon@gmail.com',
    license='Apache Software License 2.0',
    url='https://github.com/Top-Q/difido-reports/tree/master/binders/difido-pytest-plugin/pytest-difido',
    description='PyTest plugin for generating Difido reports',
    long_description=read('README.rst'),
    long_description_content_type='text/x-rst',
    py_modules=['difido_definitions'],
    python_requires='>=3.5',
    include_package_data=True,
    install_requires=['pytest>=4.0.0', 'requests>=2.20.0', 'requests-toolbelt>=0.8.0','rich >=10.14.0'],
    packages= ["difido"],
    classifiers=[
        'Development Status :: 4 - Beta',
        'Framework :: Pytest',
        'Intended Audience :: Developers',
        'Topic :: Software Development :: Testing',
        'Programming Language :: Python',
        'Programming Language :: Python :: 3',
        'Programming Language :: Python :: 3.5',
        'Programming Language :: Python :: 3.6',
        'Programming Language :: Python :: 3.7',
        'Programming Language :: Python :: 3.8',
        'Programming Language :: Python :: 3 :: Only',
        'Programming Language :: Python :: Implementation :: CPython',
        'Programming Language :: Python :: Implementation :: PyPy',
        'Operating System :: OS Independent',
        'License :: OSI Approved :: Apache Software License',
    ],
    entry_points={
        'pytest11': [
            'difido = difido.plugin',
        ],
    },
)
