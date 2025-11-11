'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useChat } from '@/lib/context';
import UserList from '@/components/UserList';
import ChatInterface from '@/components/ChatInterface';
import { LogOut, Users } from 'lucide-react';

export default function MemberDashboard() {
  const router = useRouter();
  const { currentUser, users, selectedUser, setSelectedUser, logout, loadConversation, isConnected } = useChat();

  useEffect(() => {
    const savedUser = localStorage.getItem('currentUser');
    if (!savedUser) {
      router.push('/login');
      return;
    }

    try {
      const user = JSON.parse(savedUser);
      if (user.userType !== 'member') {
        alert('Access denied. Members only.');
        router.push('/login');
      }
    } catch {
      router.push('/login');
    }
  }, [router]);

  const handleSelectUser = (user: typeof selectedUser) => {
    if (user) {
      setSelectedUser(user);
      loadConversation(user);
    }
  };

  const handleLogout = () => {
    if (confirm('Are you sure you want to logout?')) {
      logout();
    }
  };

  if (!currentUser || currentUser.userType !== 'member') {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4" />
          <p className="text-gray-600">Loading member dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="h-screen flex flex-col bg-gray-50">
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="px-6 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="bg-blue-600 p-2 rounded-lg">
              <Users className="w-6 h-6 text-white" />
            </div>
            <div>
              <h1 className="text-xl font-bold text-gray-900">Member Dashboard</h1>
              <p className="text-sm text-gray-500">{currentUser.username}</p>
            </div>
          </div>
          
          <div className="flex items-center gap-4">
            <div className="text-sm">
              <div className="flex items-center gap-2">
                <div className={`w-2 h-2 rounded-full ${isConnected ? 'bg-green-500' : 'bg-red-500'}`} />
                <span className="text-gray-600">{isConnected ? 'Connected' : 'Disconnected'}</span>
              </div>
            </div>
            
            <button
              onClick={handleLogout}
              className="flex items-center gap-2 px-4 py-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
            >
              <LogOut className="w-5 h-5" />
              <span className="font-medium">Logout</span>
            </button>
          </div>
        </div>
      </header>

      <div className="flex-1 flex overflow-hidden">
        <UserList
          users={users.filter(u => u.userType === 'admin')}
          selectedUser={selectedUser}
          onSelectUser={handleSelectUser}
          title="Administrators"
        />
        <ChatInterface selectedUser={selectedUser} />
      </div>
    </div>
  );
}
