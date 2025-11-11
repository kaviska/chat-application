import { User } from '@/types';
import { Users as UsersIcon, Circle } from 'lucide-react';

interface UserListProps {
  users: User[];
  selectedUser: User | null;
  onSelectUser: (user: User) => void;
  title: string;
}

export default function UserList({ users, selectedUser, onSelectUser, title }: UserListProps) {
  return (
    <div className="w-80 bg-white border-r border-gray-200 flex flex-col">
      <div className="p-4 border-b border-gray-200">
        <div className="flex items-center gap-2 mb-2">
          <UsersIcon className="w-5 h-5 text-gray-600" />
          <h2 className="text-lg font-semibold text-gray-900">{title}</h2>
        </div>
        <p className="text-sm text-gray-500">{users.length} total</p>
      </div>

      <div className="flex-1 overflow-y-auto">
        {users.length === 0 ? (
          <div className="p-4 text-center text-gray-500">
            <UsersIcon className="w-12 h-12 mx-auto mb-2 text-gray-300" />
            <p>No users available</p>
          </div>
        ) : (
          <div className="divide-y divide-gray-100">
            {users.map((user) => (
              <button
                key={user.email}
                onClick={() => onSelectUser(user)}
                className={`w-full p-4 text-left hover:bg-gray-50 transition-colors ${
                  selectedUser?.email === user.email ? 'bg-blue-50' : ''
                }`}
              >
                <div className="flex items-center justify-between">
                  <div className="flex-1 min-w-0">
                    <p className="font-medium text-gray-900 truncate">
                      {user.username}
                    </p>
                    <p className="text-sm text-gray-500 truncate">{user.email}</p>
                  </div>
                  <Circle
                    className={`w-3 h-3 flex-shrink-0 ml-2 ${
                      user.status === 'online'
                        ? 'text-green-500 fill-green-500'
                        : 'text-gray-300 fill-gray-300'
                    }`}
                  />
                </div>
              </button>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
