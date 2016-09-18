import serial
import serial.tools.list_ports
import copy

import numpy as np
import math
import random


class AsciiSerial:
    def __init__(self):
        self._channelsList = {'graph1': None, 'graph2': None, 'graph3': None, 'graph4': None}
        self._enChannels = {'graph1': False, 'graph2': False, 'graph3': False, 'graph4': False}
        self.channelToID = {
            'POSITION':     0,
            'TRAJECTORY':   1,
            'PID_V_G':      2,
            'PID_V_D':      3,
            'PID_TRANS':    4,
            'BLOCKING_M_G': 5,
            'BLOCKING_M_D': 6,
            'STOPPING_MGR': 7,
            'DIRECTION':    8,
            'SENSORS':      9}

        self.linesToSend = []
        self.receivedLines_main = []
        self.receivedLines_warning = []
        self.receivedLines_error = []

        self.serial = serial.Serial()
        self.incomingLine = ""

        # Format des données :
        # {'graphN': {'data': {'lineName': lineData, ...}, 'shape': String}
        #
        # 'shape' peut être :
        #   "line"   : ligne continue reliant chaque point
        #   "scatter": nuage de points (x,y) indépendants
        #   "line-scatter: nuage de points (x,y) reliés entre eux
        #
        # Pour chaque 'shape', 'lineData' a une forme différente :
        #   "line"   : tableau à 1 dimension représentant les coordonnées y de chaque point
        #   "scatter": tableau t à 2 dimensions. t[0] est un tableau représentant x pour chaque point. t[1] représente y
        #   "line-scatter": idem que 'scatter'

        self.graphData = {'graph1': {'data': None, 'shape': None},
                          'graph2': {'data': None, 'shape': None},
                          'graph3': {'data': None, 'shape': None},
                          'graph4': {'data': None, 'shape': None}}

        self.phase = np.linspace(0, 10 * np.pi, 100)
        self.index = 0

    @staticmethod
    def scanPorts():
        return list(serial.tools.list_ports.comports())

    def open(self, port):
        self.serial.port = port.split(" ")[0]
        self.serial.open()

    def close(self):
        self.serial.close()

    def communicate(self):
        if self.serial.is_open:
            for line in self.linesToSend:
                self.serial.write(line.encode('ascii'))
            self.linesToSend.clear()

            nbB = self.serial.in_waiting
            if nbB > 0:
                self.incomingLine += self.serial.read(nbB).decode()

            newLineIndex = self.incomingLine.find('\n')
            while newLineIndex != -1:
                self.addLineToProperList(self.incomingLine[0:newLineIndex+1])
                self.incomingLine = self.incomingLine[newLineIndex+1:]
                newLineIndex = self.incomingLine.find('\n')

    def addLineToProperList(self, line):
        if len(line) > 5 and line[0:6] == "_data_":
            try:
                self.addGraphData(line[6:])
            except ValueError:
                self.receivedLines_main.append(line)
        elif len(line) > 8 and line[0:9] == "_warning_":
            self.receivedLines_warning.append(line[9:])
        elif len(line) > 6 and line[0:7] == "_error_":
            errorLine = "#" + line[7:8] + "# " + line[9:]
            self.receivedLines_error.append(errorLine)
        else:
            self.receivedLines_main.append(line)

    def addGraphData(self, strData):
        data = strData.split("_")
        idChannel = int(data[0])
        channel = None
        for c, i in self.channelToID.items():
            if i == idChannel:
                channel = c
                break
        if channel is None:
            raise ValueError

        graph = None
        for g in ['graph1', 'graph2', 'graph3', 'graph4']:
            if self._channelsList[g] == channel:
                graph = g
                break
        if graph is None:
            raise ValueError

        values = []
        for strValue in data[1:]:
            values.append(float(strValue))
        if len(values) == 0:
            raise ValueError

        if channel == 'POSITION':
            if len(values) != 3:
                raise ValueError
            self.graphData[graph]['data']['p'][0].append(values[0])
            self.graphData[graph]['data']['p'][1].append(values[1])
        elif channel == 'TRAJECTORY':
            pass #todo
        elif channel == 'PID_V_G':
            if len(values) != 3:
                raise ValueError
            self.graphData[graph]['data']['setPoint'].append(values[0])
            self.graphData[graph]['data']['value'].append(values[1])
            self.graphData[graph]['data']['output'].append(values[2])
        elif channel == 'PID_V_D':
            if len(values) != 3:
                raise ValueError
            self.graphData[graph]['data']['setPoint'].append(values[0])
            self.graphData[graph]['data']['value'].append(values[1])
            self.graphData[graph]['data']['output'].append(values[2])
        elif channel == 'PID_TRANS':
            if len(values) != 3:
                raise ValueError
            self.graphData[graph]['data']['setPoint'].append(values[0])
            self.graphData[graph]['data']['value'].append(values[1])
            self.graphData[graph]['data']['output'].append(values[2])
        elif channel == 'BLOCKING_M_G':
            if len(values) != 3:
                raise ValueError
            self.graphData[graph]['data']['aimSpeed'].append(values[0])
            self.graphData[graph]['data']['realSpeed'].append(values[1])
            self.graphData[graph]['data']['isBlocked'].append(values[2])
        elif channel == 'BLOCKING_M_D':
            if len(values) != 3:
                raise ValueError
            self.graphData[graph]['data']['aimSpeed'].append(values[0])
            self.graphData[graph]['data']['realSpeed'].append(values[1])
            self.graphData[graph]['data']['isBlocked'].append(values[2])
        elif channel == 'STOPPING_MGR':
            if len(values) != 2:
                raise ValueError
            self.graphData[graph]['data']['speed'].append(values[0])
            self.graphData[graph]['data']['isStopped'].append(values[1])
        elif channel == 'DIRECTION':
            pass #todo
        elif channel == 'SENSORS':
            pass #todo
        else:
            raise ValueError

    def setEnabledChannels(self, competeConfig):
        newChannelsList = {'graph1': competeConfig['graph1']['channel'],
                           'graph2': competeConfig['graph2']['channel'],
                           'graph3': competeConfig['graph3']['channel'],
                           'graph4': competeConfig['graph4']['channel']}
        newEnabledList = {'graph1': competeConfig['graph1']['enable'],
                          'graph2': competeConfig['graph2']['enable'],
                          'graph3': competeConfig['graph3']['enable'],
                          'graph4': competeConfig['graph4']['enable']}

        commandLines = []
        graphs = ['graph1', 'graph2', 'graph3', 'graph4']
        for graph in graphs:
            if newChannelsList[graph] != self._channelsList[graph]:
                if self._enChannels[graph]:
                    commandLines.append(self.enableChannel(self._channelsList[graph], False))
            else:
                if newEnabledList[graph] != self._enChannels[graph]:
                    if not newEnabledList[graph]:
                        commandLines.append(self.enableChannel(self._channelsList[graph], False))

        for graph in graphs:
            if newChannelsList[graph] != self._channelsList[graph]:
                if newEnabledList[graph]:
                    self.resetGraphData(graph, newChannelsList[graph])
                    commandLines.append(self.enableChannel(newChannelsList[graph], True))
            else:
                if newEnabledList[graph] != self._enChannels[graph]:
                    if newEnabledList[graph]:
                        self.resetGraphData(graph, newChannelsList[graph])
                        commandLines.append(self.enableChannel(self._channelsList[graph], True))

        self._channelsList = newChannelsList
        self._enChannels = newEnabledList
        return commandLines

    def enableChannel(self, channel, enable):
        commandLine = ""
        if channel in self.channelToID:
            if enable:
                commandLine = "logon "
            else:
                commandLine = "logoff "
            commandLine += str(self.channelToID[channel])
            commandLine += '\n'
            self.addLinesToSend([commandLine])
        return commandLine

    def resetGraphData(self, graph, channel):
        if channel == 'POSITION':
            self.graphData[graph]['data'] = {"p": [[], []]}
            self.graphData[graph]['shape'] = 'line-scatter'

        elif channel == 'TRAJECTORY':
            pass #todo

        elif channel == 'PID_V_G':
            self.graphData[graph]['data'] = {'setPoint': [], 'value': [], 'output': []}
            self.graphData[graph]['shape'] = 'line'

        elif channel == 'PID_V_D':
            self.graphData[graph]['data'] = {'setPoint': [], 'value': [], 'output': []}
            self.graphData[graph]['shape'] = 'line'

        elif channel == 'PID_TRANS':
            self.graphData[graph]['data'] = {'setPoint': [], 'value': [], 'output': []}
            self.graphData[graph]['shape'] = 'line'

        elif channel == 'BLOCKING_M_G':
            self.graphData[graph]['data'] = {'aimSpeed': [], 'realSpeed': [], 'isBlocked': []}
            self.graphData[graph]['shape'] = 'line'

        elif channel == 'BLOCKING_M_D':
            self.graphData[graph]['data'] = {'aimSpeed': [], 'realSpeed': [], 'isBlocked': []}
            self.graphData[graph]['shape'] = 'line'

        elif channel == 'STOPPING_MGR':
            self.graphData[graph]['data'] = {'speed': [],'isStopped': []}
            self.graphData[graph]['shape'] = 'line'

        elif channel == 'DIRECTION':
            pass #todo

        elif channel == 'SENSORS':
            pass #todo

    def getLines_main(self):
        lines = copy.deepcopy(self.receivedLines_main)
        self.receivedLines_main.clear()
        return lines

    def getLines_warning(self):
        lines = copy.deepcopy(self.receivedLines_warning)
        self.receivedLines_warning.clear()
        return lines

    def getLines_error(self):
        lines = copy.deepcopy(self.receivedLines_error)
        self.receivedLines_error.clear()
        return lines

    def addLinesToSend(self, lines):
        self.linesToSend += lines

    def getAllData(self):
        y = np.multiply(np.sin(np.linspace(0, 6 * np.pi, 100) + self.phase[self.index]), self.index/20)
        y2 = np.multiply(np.sin(np.linspace(0, 6 * np.pi, 100) + (self.phase[self.index] + 0.1)), self.index/30)
        self.index = int(math.fmod((self.index + 1), len(self.phase)))
        return {'graph1': {'data': {'pwm': y, 'bite': y2}, 'shape': 'line'},
                'graph2': {'data':
                               {'traj': [[0,1,5*random.random(),9,12,6,3],[0,2,3,6*random.random(),7,2,-3]],
                                'bite': [[0, 2, 4 * random.random(), 9, 12, 7, 3],
                                         [3, 2, 3, 5 * random.random(), 3, 2, -1]]},
                           'shape': 'scatter'},
                'graph3': {'data': {}, 'shape': 'line'},
                'graph4': {'data': {}, 'shape': 'line'}
                }
