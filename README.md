# MetaTrade Gateway 
基于WebSocket和Stomp服务的区块链广播网关
## 模型介绍
### 模型数据结构
#### 交易信息结构
MetaTrade是记录交易数据的一种服务，基本的单元是`Trade`：
```json
{
	"senderAddress": "0000000000000000",
	"receiverAddress": "FFFFFFFFFFFFFFFF",
	"amount": 10.00,
	"commission": 0.01,
	"timestamp": 1682177056
}
```
在网关服务中，地址是表示交易角色的唯一方式，**Todo: 地址是如何产生的？**
`commission`手续费是用于提供给POW证明者的奖励，由`sender`提供

#### 交易信息集合结构
交易信息集合包括区块(`Block`)、类区块（`rawBlock`）和当前交易池

MetaTrade将所有交易信息集合分为三个层次：

- 已经上链，即通过POW验证的区块，按照上链顺序组织成列表，称为`chain`
- 已经被打包，但没有被POW验证的区块，称为RawBlock，按照打包顺序组织成队列，称为`rawBlockDeque`
- 未被打包，还在交易池中的交易信息集合， 按照提交顺序组织成列表，称为`tradeList`

##### 区块 Block
已经上链的区块存储以下信息：

- `prevHash`：链中前一个区块的Hash值
- `merkleHash`：本区块中所有交易信息形成的MerkelHash
- `proofLevel`：本区块POW时的证明难度等级
- `proof`：本区块POW时的随机数结果
- `blockBody`：上一个区块的`Rewarded`交易和打包时的交易信息集合

[Block的Hash值如何确定？](#POW证明)

##### 类区块 RawBlock
类区块仅仅存储打包时的交易记录集合和打包时的`proofLevel`

### 模型逻辑
#### 提交Trade
任意节点都可以提交`Trade`消息，网关会将收到的消息进行转发，以便网络中的所有节点都能收到这条交易信息

#### Spawn区块
在网络中，由交易池打包生成类区块的过程称为`Spawn`，何时决定`Spawn`由网关进行控制

当网关判断需要`Spawn`时，会向所有节点广播消息，指示节点将自己的交易池中的所有交易信息打包类区块，打包后清空交易池

一般来说，不会出现空的类区块，即当前交易池中如果为空，则网关不会进行`Spawn`操作

#### POW证明
在网络中只能有针对一个区块的POW，因此所有涉及POW提交和验证的区块都指的是当前`rawBlockDeque`中的第一个`rawBlock`

网关维护者当前的`ProofLevel`，该值可能会随着网络变化而发生改变（**Todo: 当前固定**）

要进行POW证明，首先需要获取当前链最后一个区块的Hash值（`prevHash`）和待证明的类区块（即Deque的第一个类区块）的元数据信息，这包括：

- 类区块内所有交易信息的`merkleHash`
- 类区块的`proofLevel`

当节点计算出这三者的值后，可以进行POW工作，节点需要通过计算获得一个数字，使得

$$
\mathrm{HASH}(Block)\ =\ \mathrm{SHA256}(\mathrm{SHA256}(prevHash \cdot merkleHash \cdot proofLevel \cdot proof))\ =\ 0000\cdots
$$

$a \cdot b$为字符串的连接符号，结果的前导0数量应与`proofLevel`相同

当节点计算出这个`proof`，可以向网关提交消息，网关会进行转发（略去证明节点的地址），若该节点的证明获得网络中半数及以上的节点的同意时，该区块会被上链

`proof`的提交按照节点提交网关的时间顺序优先，即两个节点先后提交了相同的`proof`，网关只会记录先提交的地址作为POW的奖励发放对象

#### POW奖励
成功进行POW的节点地址将会收获该区块内的所有手续费`Commission`作为POW的奖励（该笔交易无手续费），同时网关还会提供额外固定的奖励（`fixedMineReward`），作为增加货币流通量的渠道之一

同时这笔交易记录会被写入下一个类区块（即被POW的区块的下一个类区块）的交易记录的第一条，以保证每个上链的区块的第一笔交易都是POW奖励的交易

### 初始化与配置
初始化操作包括读取配置、创造`Genesis Block`两个步骤

#### 读取配置
```
metatrade-gateway.admin-address=0000000000000000
metatrade-gateway.broadcastAddress=FFFFFFFFFFFFFFFF
metatrade-gateway.fixedMineReward=5.00
metatrade-gateway.initCoins=100
metatrade-gateway.initHash=1
metatrade-gateway.proofLevel=4
metatrade-gateway.genesisProofLevel=4
metatrade-gateway.spawnSecond=600
```

`metatrade-gateway.proofLevel`和`metatrade-gateway.spawnSecond`在当前固定，后续可能随着网络的变化动态调整

#### 创造`Genesis Block`
`Genesis Block`是区块链服务中的第一个上链区块

在这个区块中，`prevHash`和`proofLevel`由配置`metatrade-gateway.initHash`和`metatrade-gateway.genesisProofLevel`指定，只存储一条交易记录，即由`admin-address`发送给`broadcastAddress`的`initCoins`的交易记录（无手续费）

`broadcastAddress`地址代表任何网络中的节点，这意味着，任何新加入网络的节点都会获得`initCoins`的货币作为账户初始余额

## Stomp服务
### Stomp端点地址
`ws://host:7285/meta-trade/stomp`
### 消息格式
#### Trade
```json
{
	"senderAddress": "0000000000000000",
	"receiverAddress": "FFFFFFFFFFFFFFFF",
	"amount": 10.00,
	"commission": 0.01,
	"timestamp": 1682177056
}
```
#### SyncMessage
```json
{
	"chain":[
		{
            "prevHash": "1",
            "merkleHash": "123",
            "proofLevel": 4,
            "proof": 25,
            "blockBody": [
                {
                    "senderAddress": "abc",
                    "receiverAddress": "def",
                    "amount": 10.00,
                    "commission": 0.01,
                    "timestamp": 1682177056
                },
                {
                    "senderAddress": "123",
                    "receiverAddress": "456",
                    "amount": 15.00,
                    "commission": 0.01,
                    "timestamp": 1682577056
                }
            ]
        },
        
        {
            "prevHash": "12345",
            "merkleHash": "123",
            "proofLevel": 4,
            "proof": 25,
            "blockBody": [
                {
                    "senderAddress": "abc",
                    "receiverAddress": "def",
                    "amount": 10.00,
                    "commission": 0.01,
                    "timestamp": 1682177056
                },
                {
                    "senderAddress": "123",
                    "receiverAddress": "456",
                    "amount": 15.00,
                    "commission": 0.01,
                    "timestamp": 1682577056
                }
            ]
        }
	],
	
	"rawBlocks":[
		{
			"proofLevel": 4,
			"blockBody": [
                {
                    "senderAddress": "abc",
                    "receiverAddress": "def",
                    "amount": 10.00,
                    "commission": 0.01,
                    "timestamp": 1682177056
                },
                {
                    "senderAddress": "123",
                    "receiverAddress": "456",
                    "amount": 15.00,
                    "commission": 0.01,
                    "timestamp": 1682577056
                }
            ]
		},
		
		{
			"proofLevel": 5,
			"blockBody": [
                {
                    "senderAddress": "abc",
                    "receiverAddress": "def",
                    "amount": 10.00,
                    "commission": 0.01,
                    "timestamp": 1682177056
                },
                {
                    "senderAddress": "123",
                    "receiverAddress": "456",
                    "amount": 15.00,
                    "commission": 0.01,
                    "timestamp": 1682577056
                }
            ]
		}
	],
	
	"tradeList":[
		{
            "senderAddress": "abc",
            "receiverAddress": "def",
            "amount": 10.00,
            "commission": 0.01,
            "timestamp": 1682177056
        },
        
        {
            "senderAddress": "123",
            "receiverAddress": "456",
            "amount": 15.00,
            "commission": 0.01,
            "timestamp": 1682577056
        }
	]
}
```

#### SpawnMessage
```json
{
	"proofLevel": 4
}
```
#### JudgeMessage
```json
{
	"proof": 25
}
```

#### SemiSyncMessage
```json
{
	"block": {
		"prevHash": "1",
		"merkleHash": "123",
		"proofLevel": 4,
		"proof": 25,
		"blockBody": [
			{
                "senderAddress": "abc",
                "receiverAddress": "def",
                "amount": 10.00,
                "commission": 0.01,
                "timestamp": 1682177056
            },
            {
                "senderAddress": "123",
                "receiverAddress": "456",
                "amount": 15.00,
                "commission": 0.01,
                "timestamp": 1682577056
            }
		]
	},
	
	"rawBlocks":[
		{
			"proofLevel": 4,
			"blockBody": [
                {
                    "senderAddress": "abc",
                    "receiverAddress": "def",
                    "amount": 10.00,
                    "commission": 0.01,
                    "timestamp": 1682177056
                },
                {
                    "senderAddress": "123",
                    "receiverAddress": "456",
                    "amount": 15.00,
                    "commission": 0.01,
                    "timestamp": 1682577056
                }
            ]
		},
		
		{
			"proofLevel": 5,
			"blockBody": [
                {
                    "senderAddress": "abc",
                    "receiverAddress": "def",
                    "amount": 10.00,
                    "commission": 0.01,
                    "timestamp": 1682177056
                },
                {
                    "senderAddress": "123",
                    "receiverAddress": "456",
                    "amount": 15.00,
                    "commission": 0.01,
                    "timestamp": 1682577056
                }
            ]
		}
	],
	
	"tradeList":[
		{
            "senderAddress": "abc",
            "receiverAddress": "def",
            "amount": 10.00,
            "commission": 0.01,
            "timestamp": 1682177056
        },
        
        {
            "senderAddress": "123",
            "receiverAddress": "456",
            "amount": 15.00,
            "commission": 0.01,
            "timestamp": 1682577056
        }
	]
}
```

#### ProofMessage
```json
{
	"block": {
		"prevHash": "1",
		"merkleHash": "123",
		"proofLevel": 4,
		"proof": 25,
		"blockBody": [
			{
                "senderAddress": "abc",
                "receiverAddress": "def",
                "amount": 10.00,
                "commission": 0.01,
                "timestamp": 1682177056
            },
            {
                "senderAddress": "123",
                "receiverAddress": "456",
                "amount": 15.00,
                "commission": 0.01,
                "timestamp": 1682577056
            }
		]
	},
	
	"address": "proofer-address"
}
```

#### AgreeMessage
```json
{
	"address": "your-address",
	"proof": 25
}
```

### API接口
#### Subscribe（需要节点执行Subscribe指令）
##### 初始化请求订阅 `/meta-trade/post/init`
该订阅立即回调
返回`SyncMessage`

##### 交易广播 `/meta-trade/subscribe/trade`
当网关收到了一条交易信息，网关进行转发广播，所有节点都会收到这条交易的广播消息
返回`Trade`

##### 打包广播 `/meta-trade/subscribe/spawn`
当网关判断此时需要打包区块时，所有节点都会收到这条消息（保持网络中的节点块趋于一致）
返回`SpawnMessage`

##### 提交验证广播`/meta-trade/subscribe/judge`
当网关收到了一条POW消息后，所有节点都会收到网关处理后的消息
返回`JudgeMessage`

##### 上链广播`/meta-trade/subscribe/semi-sync`
当网络内半数以上节点同意该POW后，新的区块上链，所有节点都会收到新的同步信息
返回`SemiSyncMessage`

##### 同步订阅`/meta-trade/user/sync`
节点主动发送同步消息后，将会在这个订阅中收到同步的消息
返回`SyncMessage`

#### Post（需要节点执行Send指令）
##### 交易消息 `/meta-trade/post/trade`
节点需要广播交易时，向网关发送`Trade`消息
##### 提交POW消息`/meta-trade/post/proof`
节点需要广播自己的POW时，向网关发送`ProofMessage`消息
##### 同意POW消息`/meta-trade/post/agree`
节点如果同意之前收到的POW消息时，向网关发送`AgreeMessage`消息

