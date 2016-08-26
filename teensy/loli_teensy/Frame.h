#ifndef _FRAME_h
#define _FRAME_h

#include <vector>

enum FrameType
{
	NEW_ORDER		= 0xFF,
	EXECUTION_BEGIN	= 0xFC,
	STATUS_UPDATE	= 0xFA,
	EXECUTION_END	= 0xFB,
	END_ORDER		= 0xFE,
	VALUE_REQUEST	= 0xFD,
	VALUE_ANSWER	= 0xF9
};

enum OrderType
{
	IMMEDIATE_ORDER,
	LONG_ORDER
};


class Frame
{
public:
	Frame() // Constructeur par défaut, volontairement inutilisable.
	{
		frameValid = false;
	}

	Frame(const std::vector<uint8_t> & frame)
	{
		if (frame.size() < 4)
		{
			frameValid = false;
			return;
		}
		else if (frame.at(1) != frame.size())
		{
			frameValid = false;
			return;
		}

		if (frame.at(0) >= 0xF9)
		{
			type = (FrameType)frame.at(0);
		}
		else
		{
			frameValid = false;
			return;
		}

		orderType = findOrderType(type);

		id = frame.at(2);
		if (frame.size() > 4)
		{
			if (type == EXECUTION_BEGIN || type == END_ORDER)
			{
				frameValid = false;
				return;
			}
			order = frame.at(3);
			for (unsigned int i = 4; i < frame.size() - 1; i++)
			{
				data.push_back(frame.at(i));
			}
		}
		else if(type != EXECUTION_BEGIN && type != END_ORDER)
		{
			frameValid = false;
			return;
		}
		

		updateLengthAndChecksum();
		if (checksum == frame.at(frame.size() - 1))
			frameValid = true;
		else
			frameValid = false;
	}

	Frame(FrameType newType, uint8_t newId, uint8_t newOrder, const std::vector<uint8_t> & newData)
	{
		type = newType;
		orderType = findOrderType(newType);
		id = newId;
		order = newOrder;
		data = newData;
		updateLengthAndChecksum();
		frameValid = (type != EXECUTION_BEGIN && type != END_ORDER);
	}

	Frame(FrameType newType, uint8_t newId)
	{
		type = newType;
		orderType = findOrderType(newType);
		id = newId;
		updateLengthAndChecksum();
		frameValid = (type == EXECUTION_BEGIN || type == END_ORDER);
	}

	bool isFrameValid() const
	{
		return frameValid;
	}

	void setData(const std::vector<uint8_t> & newData)
	{
		data = newData;
		updateLengthAndChecksum();
	}

	void setFrameType(FrameType newFrameType)
	{
		if (orderType == findOrderType(newFrameType))
		{
			type = newFrameType;
			updateLengthAndChecksum();
		}
		else
		{
			//TODO : throw error
		}
	}

	std::vector<uint8_t> const & getData() const
	{
		return data;
	}

	FrameType getFrameType() const
	{
		return type;
	}

	OrderType getOrderType() const
	{
		return orderType;
	}

	uint8_t getID() const
	{
		return id;
	}

	uint8_t getOrder() const
	{
		return order;
	}

	std::vector<uint8_t> getFrameVect() const
	{
		std::vector<uint8_t> output;
		output.push_back((uint8_t)type);
		output.push_back(length);
		output.push_back(id);
		if (type != EXECUTION_BEGIN && type != END_ORDER)
		{
			output.push_back(order);
			for (unsigned int i = 0; i < data.size(); i++)
			{
				output.push_back(data.at(i));
			}
		}
		output.push_back(checksum);
		return output;
	}

private:
	OrderType findOrderType(FrameType ft)
	{
		if (ft == VALUE_REQUEST || ft == VALUE_ANSWER)
			return IMMEDIATE_ORDER;
		else
			return LONG_ORDER;
	}

	void updateLengthAndChecksum()
	{
		length = 4 + data.size();
		if (type != EXECUTION_BEGIN && type != END_ORDER)
		{
			length++;
		}

		checksum = 0;
		checksum += (uint8_t)type;
		checksum += length;
		checksum += id;
		if (type != EXECUTION_BEGIN && type != END_ORDER)
		{
			checksum += order;
		}
		for (unsigned int i = 0; i < data.size(); i++)
		{
			checksum += data.at(i);
		}
	}

	FrameType type;
	OrderType orderType;
	uint8_t length;
	uint8_t id;
	uint8_t order;
	uint8_t checksum;
	std::vector<uint8_t> data;
	bool frameValid;
};


#endif

