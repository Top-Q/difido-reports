# -*- coding: utf-8 -*-


def test_command_line_arguments(testdir):
    """Make sure that all command line arguments are accessible from the config"""

    # create a temporary pytest test module
    testdir.makepyfile("""
        from difido.reporters import config
        
        def test_config():
            assert config.host == "foo"
            assert config.port == 1111
            assert config.output_folder == "bar"
            assert config.execution_description == "zoo"
            assert config.reporters == "difido.reporters.ConsoleReporter"
    """)

    # run pytest with the following cmd args
    result = testdir.runpytest(
        '--df_host=foo',
        '--df_port=1111',
        '--df_output=bar',
        '--df_description=zoo',
        '--df_reporters=difido.reporters.ConsoleReporter',
        '-v'
    )

    # fnmatch_lines does an assertion internally
    result.stdout.fnmatch_lines([
        '*::test_config PASSED*',
    ])

    # make sure that that we get a '0' exit code for the testsuite
    assert result.ret == 0


def test_help_message(testdir):
    """Make sure that all settings are visible from the help"""
    result = testdir.runpytest(
        '--help',
    )
    # fnmatch_lines does an assertion internally
    result.stdout.fnmatch_lines([
        'difido:',
        '*--df_host=DEST_DF_HOST',
        '*Difido server host or ip',
        '*--df_port=DEST_DF_PORT',
        '*Difido server port address',
        '*--df_description=DEST_DF_DESCRIPTION',
        '*Difido execution description',
        '*--df_output=DEST_DF_OUTPUT',
        '*Reports output folder',
    ])


def test_ini_setting(testdir):
    """Make sure that all ini settings are accessible from the config"""
    testdir.makeini("""
        [pytest]
        df_host = 192.168.10.1
        df_port = 1111
        df_reporters = bar
        df_description = zoo
        df_output = foo
    """)

    testdir.makepyfile("""
        import pytest
        

        @pytest.fixture
        def conf(request):
            return request.config

        def test_ini_config(conf):
            assert conf.getini('df_host') == '192.168.10.1'
            assert conf.getini('df_port') == '1111'
            assert conf.getini('df_reporters') == 'bar'
            assert conf.getini('df_description') == 'zoo'
            assert conf.getini('df_output') == 'foo'
    """)

    result = testdir.runpytest('-v')

    # fnmatch_lines does an assertion internally
    result.stdout.fnmatch_lines([
        '*::test_ini_config PASSED*',
    ])

    # make sure that that we get a '0' exit code for the testsuite
    assert result.ret == 0


def test_command_line_params_over_ini(testdir):
    """Make sure that command line parameters have priority over ini settings"""
    testdir.makeini("""
        [pytest]
        df_host = low_priority
    """)

    # create a temporary pytest test module
    testdir.makepyfile("""
        from difido.reporters import config

        def test_priority():
            assert config.host == "high_priority"
    """)

    # run pytest with the following cmd args
    result = testdir.runpytest(
        '--df_host=high_priority',
        '-v'
    )

    # fnmatch_lines does an assertion internally
    result.stdout.fnmatch_lines([
        '*::test_priority PASSED*',
    ])

    # make sure that that we get a '0' exit code for the testsuite
    assert result.ret == 0
