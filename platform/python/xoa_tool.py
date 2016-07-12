# -*- coding: UTF-8 -*-

import ZKClient
import sys

ROOT="/thrift"
ENABLE="enable"
DISABLE="disable"
ALARM_EMAIL="alarmemail"

zk_client = None
def getClient():
    global zk_client
    if zk_client != None:
        return zk_client 
    server = "localhost:2181"
    zk_client = ZKClient.ZKClient(server)  
    return zk_client

def getZkPath(serviceId,child=None,node=None):
    if node != None:
        return "%s/%s/%s/%s"%(ROOT,serviceId,child,node)
    elif child != None:
        return "%s/%s/%s"%(ROOT,serviceId,child)
    else:
        return "%s/%s"%(ROOT,serviceId)
    
def serviceExists(client,serviceId,child=None):
    node = client.exists(getZkPath(serviceId,child))
    return node != None


def checkServiceId(client,serviceId):
    msg = None
    nodes = (None,ENABLE,DISABLE,ALARM_EMAIL)
    for node in nodes:
        if not serviceExists(client,serviceId,node):
            if node == None:
                msg = "service:%s not exists!!!" % (serviceId)
            else:
                msg = "service.%s:%s not exists!!!" % (node,serviceId)
            break
        
    if msg != None:
        error_msg(msg)
        getClient().close()
        exit(1)
        
    
def Usage():
    print '\033[1;31;40m'
    print "Usage:serviceId,operator(list,create,enable,disable)"
    print '\033[0m'

def printGreenNodes(serviceId,child,nodes):
    if len(nodes) == 0:
        return 
    print '\033[1;32;40m'
    for node in nodes:
        print node,child
    print '\033[0m'

def printRedNodes(serviceId,child,nodes):
    if len(nodes) == 0:
        return 
    print '\033[1;31;40m'
    for node in nodes:
        print node,child
    print '\033[0m'

def error_msg(info):
    print '\033[1;31;40m'
    print info
    print '\033[0m'

def info_msg(info):
    print '\033[1;32;40m'
    print info
    print '\033[0m'

def listService(serviceId):
    client = getClient()
    #check the node 
    checkServiceId(client,serviceId)

    print '\033[1;34;40m'
    print "serviceId",serviceId
    print '\033[0m'
    enables = client.get_children(getZkPath(serviceId,ENABLE))
    printGreenNodes(serviceId,ENABLE,enables)

    disables = client.get_children(getZkPath(serviceId,DISABLE))
    printRedNodes(serviceId,DISABLE,disables)
    
    emails = client.get_children(getZkPath(serviceId,ALARM_EMAIL))
    printGreenNodes(serviceId,ALARM_EMAIL,emails)

def createService(serviceId):
    client = getClient()
    if serviceExists(client,serviceId):
        error_msg("%s is exists,do not create again!!"%(serviceId))
        return  
    nodes = (None,ENABLE,DISABLE,ALARM_EMAIL)
    for node in nodes:
        client.create(getZkPath(serviceId,node))
    if len(sys.argv) > 3:
        node_str = sys.argv[3]
        nodes = node_str.split(",")
        info_msg("serviceId:%s"%serviceId)
        for node in nodes:
            info_msg("%s created!"%(node))
            client.create(getZkPath(serviceId,ENABLE,node))

def enableService(serviceId):
    client = getClient()
    #check the node 
    checkServiceId(client,serviceId)
    if len(sys.argv) != 4:
        error_msg("input the serviceNode")
        return 
    node = sys.argv[3]
    if node.find(",") != -1:
        error_msg("input only one node!!!") 
        return 
    enode = getZkPath(serviceId,ENABLE,node)
    disnode = getZkPath(serviceId,DISABLE,node)
    if client.exists(disnode) != None:
        client.delete(disnode)
    if client.exists(enode) == None:
        client.create(enode)
    listService(serviceId)
    
def disableService(serviceId):
    client = getClient()
    #check the node 
    checkServiceId(client,serviceId)
    if len(sys.argv) != 4:
        error_msg("input the serviceNode")
        return 
    node = sys.argv[3]
    if node.find(",") != -1:
        error_msg("input only one node!!!") 
        return 
    enode = getZkPath(serviceId,ENABLE,node)
    disnode = getZkPath(serviceId,DISABLE,node)
    if client.exists(enode) != None:
        client.delete(enode)
    if client.exists(disnode) == None:
        client.create(disnode)
    listService(serviceId)
    
def deleteService(serviceId):
    client = getClient()
    #check the node 
    checkServiceId(client,serviceId)
    if len(sys.argv) != 4:
        error_msg("input the serviceNode")
        return 
    node = sys.argv[3]
    if node.find(",") != -1:
        error_msg("input only one node!!!") 
        return 
    enode = getZkPath(serviceId,ENABLE,node)
    disnode = getZkPath(serviceId,DISABLE,node)
    if client.exists(enode) != None:
        client.delete(enode)
    if client.exists(disnode) != None:
        client.delete(disnode)
    listService(serviceId)
        
        
if __name__ == "__main__":
    if len(sys.argv) < 3:
        Usage()
        exit(1)
    serviceId = sys.argv[1]
    operator = sys.argv[2]
    
    if  operator == "list":
        listService(serviceId) 
    elif  operator == "create":
        createService(serviceId) 
    elif  operator == "enable":
        enableService(serviceId) 
    elif  operator == "disable":
        disableService(serviceId) 
    elif  operator == "delete":
        deleteService(serviceId)
    else:
        Usage()
        exit(1)
    
    #end!!
    getClient().close()
    
    
    

