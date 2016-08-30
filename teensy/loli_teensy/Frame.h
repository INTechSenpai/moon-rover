#ifndef _FRAME_h
#define _FRAME_h

#include <vector>
#include <Printable.h>

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


class Frame : public Printable
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

		size_t dataBegin;
		if (type == NEW_ORDER || type == VALUE_REQUEST)
		{// Présence du champ "id order"
			if (frame.size() < 5)
			{
				frameValid = false;
				return;
			}
			else
			{
				order = frame.at(3);
				dataBegin = 4;
			}
		}
		else
		{// Pas de champ "id order"
			dataBegin = 3;
		}

		for (size_t i = dataBegin; i < frame.size() - 1; i++)
		{
			data.push_back(frame.at(i));
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
		frameValid = (type == NEW_ORDER || type == VALUE_REQUEST);
	}

	Frame(FrameType newType, uint8_t newId, const std::vector<uint8_t> & newData)
	{
		type = newType;
		orderType = findOrderType(newType);
		id = newId;
		data = newData;
		updateLengthAndChecksum();
		frameValid = (type != NEW_ORDER && type != VALUE_REQUEST);
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
		if (type == NEW_ORDER || type == VALUE_REQUEST)
		{
			output.push_back(order);
		}
		for (size_t i = 0; i < data.size(); i++)
		{
			output.push_back(data.at(i));
		}
		output.push_back(checksum);
		return output;
	}

	size_t printTo(Print& p) const
	{
		size_t n = 0;
		if (frameValid)
		{
			n += p.print("T:");
			n += p.print(type, HEX);
			n += p.print(" id:");
			n += p.print(id, HEX);
			if (type == NEW_ORDER || type == VALUE_REQUEST)
			{
				n += p.print(" order:");
				n += p.print(order);
			}
			if (data.size() > 0)
			{
				n += p.print(" data:");
			}
			for (size_t i = 0; i < data.size(); i++)
			{
				n += p.print(data.at(i), HEX);
				n += p.print(" ");
			}
		}
		return n;
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
		if (type == NEW_ORDER || type == VALUE_REQUEST)
		{
			length++;
		}

		checksum = 0;
		checksum += (uint8_t)type;
		checksum += length;
		checksum += id;
		if (type == NEW_ORDER || type == VALUE_REQUEST)
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

